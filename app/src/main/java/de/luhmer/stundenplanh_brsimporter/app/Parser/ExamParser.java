package de.luhmer.stundenplanh_brsimporter.app.Parser;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import de.luhmer.stundenplanh_brsimporter.app.Model.ExamItem;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamRegistrationItem;


/**
 * Created by David on 04.07.2014.
 */
public class ExamParser {
    private static final String TAG = "ExamParser";
    /*
    ABP - Abmeldesperre FB 05
	An - Anerkennung
	AN - angemeldet
	ASP - Abmeldesperre Wiederholungsprüfung
	AT - Attest
	BE - bestanden
	BER - Beratungsgespräch erfolgt
	Cr - Credits
	EN - endgültig nicht bestanden
	Fv - Freivermerk
	Legende:
	LOE,LOX - Prüfungsanmeldung wurde wegen fehlender Vorleistung gelöscht
	MEP - mündliche Ergänzungsprüfung
	N - Vorleistung erbracht, bzw. nicht erforderlich
	NA - nicht angemeldet
	NB - nicht bestanden
	NE - nicht erschienen
	PFV - Freiversuch
	PVB - Notenverbesserung
	R - Rücktritt
	St - Status
	TA - Täuschung
	V - Vorleistung fehlt noch
	Vb - Vorbehalt
	Vm - Vermerk
	Vs - Versuch
	J,* - anerkannte Leistung/en einer anderen Hochschule
	** - anerkannte Prüfungsleistung aus dem Ausland
	*** - anerkannte Prüfungsleistung aus einem anderen Fachbereich der Hochschule
	**** - Bezeichnungsänderung durch Änderung in Prüfungsordnung
	S - anerkannte Prüfungsleistung aus einem anderen Studiengang der Hochschule
	*/

    public static class UnauthorizedException extends Exception {
        public UnauthorizedException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static HashMap<String, String> Login(String username, String password) throws UnauthorizedException {
        String urlParameters = "DokID=DiasSWeb&SID=&ADias2Dction=ExecLogin&UserAcc=Gast&NextAction=Basis&txtBName=" + username + "&txtKennwort=" + password;
        String url = "https://dias.fh-bonn-rhein-sieg.de/d3/SISEgo.asp?formact=Login";

        try {
            URL sisHbrs = new URL(url);

            HttpsURLConnection connection = (HttpsURLConnection) sisHbrs.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            connection.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            String page = "";

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                page += inputLine;

                if (inputLine.contains("Benutzername unbekannt oder falsches Kennwort eingegeben")) {
                    throw new UnauthorizedException("Benutzername unbekannt oder falsches Kennwort eingegeben");
                } else if (inputLine.contains("Sie haben ein falsches Kennwort eingegeben!")) {
                    throw new UnauthorizedException("Sie haben ein falsches Kennwort eingegeben!");
                }
            }
            in.close();

            return handleLinkList(page);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ExamItem> ParseExams(String urlToExams) throws UnauthorizedException, IOException {
        List<ExamItem> examItems = new ArrayList<ExamItem>();

        try {
            urlToExams = urlToExams.replace("&BfMod=Cache\" Target=\"_blank", "");

            String content = DownloadWebPage(urlToExams);

            examItems.addAll(handleExamList(content));


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        return examItems;
    }

    private static String DownloadWebPage(String urlString) throws IOException {
        String content = "";
        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                //System.out.println(inputLine);
                content += inputLine;
            }
            in.close();

            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static List<ExamRegistrationItem> ParseExamRegs(String urlToExamReg) throws UnauthorizedException, IOException {
        List<ExamRegistrationItem> examRegItems = new ArrayList<ExamRegistrationItem>();

        try {
            String content = DownloadWebPage(urlToExamReg);
            examRegItems.addAll(handleExamRegList(content));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        return examRegItems;
    }

    public static float ParseAvgGrade(String urlToPage) throws UnauthorizedException, IOException {
        float grade = 0;

        try {
            String content = DownloadWebPage(urlToPage);

            Matcher mLinks = patternParseAvgGrade.matcher(content);
            if (mLinks.find()) {
                 grade = Float.parseFloat(mLinks.group(1));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        return grade;
    }

    static Pattern patternParseAvgGrade = Pattern.compile("<a href=\"(.*?)\">(.*?)<", Pattern.DOTALL | Pattern.MULTILINE);//TODO this regex is not correct!!!

    static Pattern patternParseLinks = Pattern.compile("<a href=\"(.*?)\">(.*?)<", Pattern.DOTALL | Pattern.MULTILINE);

    static Pattern patternTableOpen = Pattern.compile("<table(.*?)</table>", Pattern.DOTALL);
    static Pattern patternTable = Pattern.compile("<table>(.*)</table>", Pattern.DOTALL | Pattern.MULTILINE);
    static Pattern patternExtractExams = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL | Pattern.MULTILINE);

    static Pattern patternPNr = Pattern.compile("PNr <strong>(.*?)<", Pattern.DOTALL|Pattern.MULTILINE);
    static Pattern patternSemester = Pattern.compile("Sem <strong>(.*?)<", Pattern.DOTALL|Pattern.MULTILINE);
    static Pattern patternStatus = Pattern.compile("Status <strong>(.*?)<", Pattern.DOTALL|Pattern.MULTILINE);
    static Pattern patternVersuch = Pattern.compile("Versuch <strong>(.*?)<", Pattern.DOTALL|Pattern.MULTILINE);
    static Pattern patternForm = Pattern.compile("Form: <strong>(.*?)<", Pattern.DOTALL|Pattern.MULTILINE);
    static Pattern patternHilfsmittel = Pattern.compile("Hilfsmittel: <strong>(.*?)<", Pattern.DOTALL|Pattern.MULTILINE);
    static Pattern patternRaum = Pattern.compile("Raum: <strong>(.*?)<", Pattern.DOTALL|Pattern.MULTILINE);
    static Pattern patternZeit = Pattern.compile("Zeit: <strong>(.*?)<", Pattern.DOTALL|Pattern.MULTILINE);

    static final String urlToExams = "https://dias.fh-bonn-rhein-sieg.de";

    public static HashMap<String, String> handleLinkList(String page) {
        HashMap<String, String> links = new HashMap<String, String>();


        Matcher mLinks = patternParseLinks.matcher(page);
        while (mLinks.find()) {
            if(mLinks.group(1).startsWith("/d3/"))
                links.put(mLinks.group(2), urlToExams + mLinks.group(1));
        }

        return links;
    }

    public static List<ExamRegistrationItem> handleExamRegList(String page) {
        List<ExamRegistrationItem> examRegItems = new ArrayList<ExamRegistrationItem>();

        int start = page.indexOf("<?xml version=\"1.0\" encoding=\"utf-16\"?>");
        int end = page.indexOf("</table></td></tr>");

        if(start != -1 && end != -1) {
            page = page.substring(start, end);

            for(String table : extractTables(page)) {
                ExamRegistrationItem examRegistration = new ExamRegistrationItem();

                examRegistration.examId = Integer.parseInt(findSingleValue(patternPNr, table));
                examRegistration.status = findSingleValue(patternStatus, table);
                examRegistration.form = findSingleValue(patternForm, table);
                examRegistration.hilfsmittel = findSingleValue(patternHilfsmittel, table);
                examRegistration.semester = parseLong(findSingleValue(patternSemester, table));
                examRegistration.versuch = parseLong(findSingleValue(patternVersuch, table));
                examRegistration.zeit = findSingleValue(patternZeit, table);
                examRegistration.raum = findSingleValue(patternRaum, table);

                examRegItems.add(examRegistration);
            }
        }

        return examRegItems;
    }

    public static Long parseLong(String value) {
        if(value != null && !value.isEmpty() && !value.equals("null")) {
            return Long.parseLong(value);
        }
        return null;
    }

    public static String findSingleValue(Pattern pattern, String needle) {
        Matcher m = pattern.matcher(needle);
        return (m.find()) ? m.group(1) : "";//null;
    }

    public static String[] extractTables(String page) {
        List<String> tables = new ArrayList<String>();
        Matcher m = patternTableOpen.matcher(page);
        while (m.find()) {
            tables.add(m.group(1));
        }
        return tables.toArray(new String[tables.size()]);
    }

    public static List<ExamItem> handleExamList(String examsList) {
        List<ExamItem> examItems = new ArrayList<ExamItem>();

        Matcher m = patternTable.matcher(examsList);
        if (m.find()) {
            String examText = m.group(1);
            Matcher mExams = patternExtractExams.matcher(examText);
            while (mExams.find()) {
                ExamItem exam = ParseSingleExam(mExams.group(1));
                if (exam != null)
                    examItems.add(exam);
            }
        }

        return examItems;
    }


    public static ExamItem ParseSingleExam(String examString) {
        examString = examString.replaceAll("<td></td>", "");
        examString = examString.replaceAll("<td align=\"left\">", "");

        examString = examString.replaceAll("</td>", "\n");
        examString = examString.replaceAll("</nobr>", "");
        examString = examString.replaceAll("<nobr>", "");
        examString = examString.replaceAll("<td.*?>", "");


        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        ExamItem exam = new ExamItem();
        try {
            String[] lines = examString.split("\n");
            exam.examId = Integer.parseInt(lines[0]);
            exam.fachName = lines[1];
            exam.note = lines[2].isEmpty() ? 0 : Double.parseDouble(lines[2].replace(",", "."));
            exam.credits = lines[3].isEmpty() ? 0 : Integer.parseInt(lines[3]);
            exam.termin = lines[4];
            exam.status = lines[5];
            exam.versuch = Integer.parseInt(lines[6]);
            exam.vermerk = lines[7];
            exam.freiVermerk = lines[8];
            exam.terminKlausur = formatter.parse(lines[10]);

            Log.d(TAG, exam.fachName + " - " + exam.note);
            //System.out.println(examString);
        } catch (Exception ex) {
            exam = null;
            //ex.printStackTrace();
        }

        return exam;
    }
}
