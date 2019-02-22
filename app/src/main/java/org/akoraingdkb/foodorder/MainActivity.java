package org.akoraingdkb.foodorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.miguelcatalan.materialsearchview.MaterialSearchView.REQUEST_VOICE;

public class MainActivity extends AppCompatActivity implements FoodAdapter.CardClickListener, BottomSheetFragment.OnAddToCartBtnClickListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private MaterialSearchView mSearchView;
    private ImageView mNavProfileImg;
    private TextView mNavUsername;
    private TextView mNavUserEmail;
    private AHBottomNavigation bottomNavigation;

    private List<FoodItem> mFoodList;
    private FoodAdapter mFoodAdapter;
    private BottomSheetFragment bottomSheetFragment;
    public static FoodItem currentItem;
    private String[] searchSuggestions = new String[10];

    private FirebaseUser currentUser;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create an instance of the FireBase User
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(getString(R.string.pref_file_name), Context.MODE_PRIVATE);

        // Check if the user is logged in and fetch user info
        if (currentUser == null) {
            storeSignOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            MainActivity.this.finish();
        }

        // Prepare the food list to be inflated
        prepareFoodList();

        Toolbar toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mSearchView = findViewById(R.id.search_view);
        bottomNavigation = findViewById(R.id.bottom_nav);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        // Configure the Toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        }

        // Create items
        AHBottomNavigationItem itemCart = new AHBottomNavigationItem(R.string.bottom_nav_cart, R.drawable.ic_shopping_cart_24dp, R.color.colorAccentFirebase);
        bottomNavigation.addItem(itemCart);

        // Configure the DrawerLayout
        configureDrawerLayout();

        // Configure the Navigation View
        configureNavigationView();

        // Update Profile info in the nav header
        updateUserInfoInNavHeader();

        // Setup the Recycler View
        mFoodAdapter = new FoodAdapter(this, mFoodList);
        mFoodAdapter.setCardClickListener(this);

        // Setup the recycler view layout manager
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mFoodAdapter);

        // Configure the Material Search View
        configureMaterialSearchView();

        configureBottomNavigation();

        bottomSheetFragment = new BottomSheetFragment();

    }

    private void configureBottomNavigation() {
        // Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(true);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        // Use colored navigation with circle reveal effect
        bottomNavigation.setColored(true);
        // Set current item programmatically
        bottomNavigation.setCurrentItem(1);
        if (FoodAdapter.count > 0)
            setCartNotification(FoodAdapter.count);
        // Set listeners
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                Toast.makeText(MainActivity.this, FoodAdapter.count + " items in cart", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    void setCartNotification(int num) {
        bottomNavigation.setNotification("" + num, 0);
    }

    private void updateUserInfoInNavHeader() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            String firstLetter = "";
            if (name != null)
                firstLetter = name.substring(0, 1);
            TextDrawable textDrawable = TextDrawable.builder().buildRound(firstLetter, getResources().getColor(R.color.royal_blue));
            mNavProfileImg.setImageDrawable(textDrawable);

            // Change the user name to the signed in user display name
            mNavUsername.setText(currentUser.getDisplayName());
            // Change the user email to the signed in email
            mNavUserEmail.setText(currentUser.getEmail());
        }
    }

    private void configureMaterialSearchView() {
        //mSearchView.setEllipsize(true);   // Not available in 1.3.0
        mSearchView.setHint("Search for an item...");
        mSearchView.setSuggestions(searchSuggestions);
        mSearchView.setVoiceSearch(true);   // Activate voice search
        mSearchView.setVoiceIcon(getResources().getDrawable(R.drawable.ic_action_voice_search));
        mSearchView.closeSearch();  // Close search view by default

        // Set Query Text Listener
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mFoodAdapter.getFilter().filter(query);
                mSearchView.closeSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mFoodAdapter.getFilter().filter(newText);
                return true;
            }
        });

        // Set Search View Listener
        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                mSearchView.showSuggestions();
            }

            @Override
            public void onSearchViewClosed() {
                hideKeyboard();
            }
        });
    }

    private void configureNavigationView() {
        // Create an object of the nav header and find the various widgets
        View navHeader = mNavigationView.getHeaderView(0);

        mNavProfileImg = navHeader.findViewById(R.id.nav_profile_img);
        mNavUsername = navHeader.findViewById(R.id.nav_user_name);
        mNavUserEmail = navHeader.findViewById(R.id.nav_email);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.nav_sign_out) {
                    // Check if user is already signed in
                    if (currentUser != null) {
                        // Sign out the user
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        // Take user to the login page
                        storeSignOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        MainActivity.this.finish();
                    }
                }

                // Add code here to update the UI based on the item selected
                // For example, swap UI fragments here
                return true;
            }
        });
    }

    private void configureDrawerLayout() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
                if (mSearchView.isSearchOpen())
                    mSearchView.closeSearch();
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                // Respond when the drawer is opened
                if (mSearchView.isSearchOpen())
                    mSearchView.closeSearch();
                invalidateOptionsMenu();

                hideKeyboard();
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                // Respond when the drawer is closed
            }

            @Override
            public void onDrawerStateChanged(int i) {
                // Respond when the drawer motion state changes
            }
        });
    }

    private void prepareFoodList() {
        mFoodList = new ArrayList<>();
        String jsonUrl = "https://jsonblob.com/api/jsonBlob/a90254ad-3670-11e9-9056-e12fdc2cad95";

        RequestQueue requestQueue;
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        // Formulate the request and handle the response
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET,
                jsonUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject foodObject = response.getJSONObject(i);
                                    mFoodList.add(new FoodItem(
                                            foodObject.getString("name"),
                                            foodObject.getString("price"),
                                            foodObject.getInt("rating"),
                                            foodObject.getString("imageUrl")
                                    ));
                                    searchSuggestions[i] = foodObject.getString("name");
                                    mFoodAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.i("JSON_Error", e.getMessage());
                                }
                            }
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }

    private int dpToPx() {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics()));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDrawerLayout.getWindowToken(), 0);
    }

    private void storeSignOut() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(getString(R.string.user_signed_out), true);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_VOICE && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (matches != null && matches.size() > 0) {
                    String searchWrd = matches.get(0);
                    if (!TextUtils.isEmpty(searchWrd)) {
                        mSearchView.setQuery(searchWrd, false);
                    }
                }
            }

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_search);
        mSearchView.setMenuItem(menuItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                invalidateOptionsMenu();
                return true;

            case R.id.menu_search:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    mDrawerLayout.closeDrawers();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawers();
        else if (mSearchView.isSearchOpen())
            mSearchView.closeSearch();
        else
            super.onBackPressed();
    }

    @Override
    public void cardClicked(View view, int position) {
        // Handle card clicked events over here
        currentItem = mFoodList.get(position);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    @Override
    public void onAddToCartBtnClick() {
        setCartNotification(FoodAdapter.count);
        Toast.makeText(this, currentItem.getName() + " added to Cart", Toast.LENGTH_SHORT).show();
    }
}
