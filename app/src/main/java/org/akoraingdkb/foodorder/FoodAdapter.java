package org.akoraingdkb.foodorder;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.MyViewHolder> implements Filterable {
    static int count;
    private Context mContext;
    private List<FoodItem> foodList;
    private List<FoodItem> filteredList;
    private CardClickListener mCardClickListener;

    FoodAdapter(Context mContext, List<FoodItem> foodList) {
        this.mContext = mContext;
        this.foodList = foodList;
        this.filteredList = foodList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int position) {
        FoodItem foodItem = filteredList.get(position);
        int cedi = 0xA2;

        // Setting food name
        myViewHolder.foodName.setText(foodItem.getName());
        myViewHolder.foodPrice.setText("GH" + Character.toString((char) cedi) + " " + foodItem.getPrice());

        // Loading food thumbnail using Glide library
        Glide.with(mContext).load(foodItem.getImageUrl()).into(myViewHolder.thumbnail);

        // Set on click listener for the overflow
        myViewHolder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(myViewHolder.overflow);
            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.food_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    void setCardClickListener (CardClickListener cardClickListener) {
        this.mCardClickListener = cardClickListener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchString = constraint.toString();
                if (TextUtils.isEmpty(searchString)) {
                    filteredList = foodList;
                } else {
                    List<FoodItem> filter = new ArrayList<>();
                    for (FoodItem food : foodList) {
                        if (food.getName().toLowerCase().startsWith(searchString.toLowerCase())) {
                            filter.add(food);
                        }
                    }
                    filteredList = filter;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<FoodItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    /*
     * Class for the view holder
     */
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView foodName, foodPrice;
        public ImageView thumbnail, overflow;

        MyViewHolder(View view) {
            super(view);
            foodName = view.findViewById(R.id.food_name);
            foodPrice = view.findViewById(R.id.food_price);
            thumbnail = view.findViewById(R.id.thumbnail);
            overflow = view.findViewById(R.id.overflow);

            thumbnail.setOnClickListener(this);
            //view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mCardClickListener != null) {
                mCardClickListener.cardClicked(v, getAdapterPosition());
            }
        }
    }

    /*
     * Interface to handle click events on the card view
     */
    interface CardClickListener {
        void cardClicked (View view, int position);
    }

    /*
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    Toast.makeText(mContext, "Added to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_rate_item:
                    Toast.makeText(mContext, "Item rated", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }

}
