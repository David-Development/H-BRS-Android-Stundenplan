package de.luhmer.stundenplanh_brsimporter.app.Adapter;

/**
 * Created by David on 05.07.2014.
 */
public class ExamRecyclerAdapter { /*extends RecyclerView.Adapter<ExamRecyclerAdapter.ViewHolder> {

    private List<ExamItem> items;
    private int itemLayout;

    public ExamRecyclerAdapter(List<ExamItem> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ExamItem item = items.get(position);
        holder.text.setText(item.getText());
        holder.image.setImageBitmap(null);
        Picasso.with(holder.image.getContext()).cancelRequest(holder.image);
        Picasso.with(holder.image.getContext()).load(item.getImage()).into(holder.image);
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }
    */
}