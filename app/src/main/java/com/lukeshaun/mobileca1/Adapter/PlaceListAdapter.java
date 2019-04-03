package com.lukeshaun.mobileca1.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.lukeshaun.mobileca1.R;

import java.util.LinkedList;

/**
 * Shows how to implement a simple Adapter for a RecyclerView.
 * Demonstrates how to add a click handler for each item in the ViewHolder.
 */
public class PlaceListAdapter extends
        RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    private final LinkedList<Place> mPlaceList;
    private final LayoutInflater mInflater;

    /* Places member variables */
    private PlacesClient mPlacesClient;

    class PlaceViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final TextView placeNameView;
        public final TextView placeAddressView;
        public final ImageView placeImageView;

        final PlaceListAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         */
        public PlaceViewHolder(View itemView, PlaceListAdapter adapter) {
            super(itemView);
            placeNameView = itemView.findViewById(R.id.placeName);
            placeAddressView = itemView.findViewById(R.id.placeAddress);
            placeImageView = itemView.findViewById(R.id.placeImage);

            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();

            // Use that to access the affected item in mPlaceList.
            Place element = mPlaceList.get(mPosition);
            // Change the place in the mPlaceList.

            mPlaceList.set(mPosition, element);
            // Notify the adapter, that the data has changed so it can
            // update the RecyclerView to display the data.
            mAdapter.notifyDataSetChanged();
        }
    }

    public PlaceListAdapter(Context context, LinkedList<Place> placeList) {
        mInflater = LayoutInflater.from(context);
        this.mPlaceList = placeList;

        mPlacesClient = Places.createClient(context);
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to
     * represent an item.
     *
     */
    @Override
    public PlaceListAdapter.PlaceViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.placelist_item, parent, false);
        return new PlaceViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(final PlaceViewHolder  holder,
                                 int position) {
        // Retrieve the data for that position.
        Place mCurrent = mPlaceList.get(position);
        // Add the data to the view holder.
        holder.placeNameView.setText(mCurrent.getName());
        holder.placeAddressView.setText(mCurrent.getAddress());

        if (mCurrent.getPhotoMetadatas() == null) {
            holder.placeImageView.setImageResource(R.drawable.comingsoon);
        } else {
            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(mCurrent.getPhotoMetadatas().get(0))
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            mPlacesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                @Override
                public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    Log.d(TAG, "SETTING IMAGE");
                    holder.placeImageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        int statusCode = apiException.getStatusCode();
                        // Handle error with given status code.
                        Log.e(TAG, "Place not found: " + exception.getMessage());
                    }
                }
            });
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mPlaceList.size();
    }
}