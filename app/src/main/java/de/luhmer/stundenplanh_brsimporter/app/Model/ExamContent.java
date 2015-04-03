package de.luhmer.stundenplanh_brsimporter.app.Model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.luhmer.stundenplanh_brsimporter.app.Helper.Base64Coder;

/**
 * Created by David on 04.07.2014.
 */
public class ExamContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<ExamItem> ITEMS = new ArrayList<ExamItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<Integer, ExamItem> ITEM_MAP = new HashMap<Integer, ExamItem>();


    private static void addItem(ExamItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.examId, item);
    }




    /**
     * A dummy item representing a piece of content.
     */
    /*
    public static class ExamItem {
        public ExamItem(String id, String content) {

        }


    }*/



    /** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException,
            ClassNotFoundException {
        byte [] data = Base64Coder.decode(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return new String( Base64Coder.encode( baos.toByteArray() ) );
    }



}
