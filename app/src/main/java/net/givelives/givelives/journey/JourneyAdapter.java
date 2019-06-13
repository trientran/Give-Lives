package net.givelives.givelives.journey;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.givelives.givelives.R;
import net.givelives.givelives.models.BloodDonor;
import net.givelives.givelives.models.BloodPost;
import net.givelives.givelives.models.OrganDonor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.MyViewHolder> {

    private List<BloodDonor> mBloodGivingList;
    private List<OrganDonor> mOrganGivingList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, date, amount;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            date = (TextView) view.findViewById(R.id.date);
            amount = (TextView) view.findViewById(R.id.amount);
        }
    }

    public JourneyAdapter(List<BloodDonor> bloodGivingList, List<OrganDonor> organGivingList) {
        this.mBloodGivingList = bloodGivingList;
        this.mOrganGivingList = organGivingList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.journey_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (position < (getItemCount() - mOrganGivingList.size())) {
            BloodDonor bloodGiving = mBloodGivingList.get(position);
            holder.title.setText(bloodGiving.postTitle);
            holder.date.setText(String.format("Date: %s", bloodGiving.transfusionDate));
            holder.amount.setText(String.format("Amount: %s ml", bloodGiving.donatedAmount));
        }
        else {
            int arrayItemPosition = position - mBloodGivingList.size();
            OrganDonor organGiving = mOrganGivingList.get(arrayItemPosition);
            holder.title.setText(organGiving.postTitle);
            holder.date.setText(String.format("Decision date: %s", timestampToDate(organGiving.commitDate)));
            holder.amount.setText("");
        }
    }

    private String timestampToDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        return formatter.format(date);
    }

    @Override
    public int getItemCount() {
        return mBloodGivingList.size() + mOrganGivingList.size();
    }
}
