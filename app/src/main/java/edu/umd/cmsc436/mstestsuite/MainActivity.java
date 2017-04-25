package edu.umd.cmsc436.mstestsuite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.umd.cmsc436.mstestsuite.data.ActionsAdapter;
import edu.umd.cmsc436.mstestsuite.model.UserManager;
import edu.umd.cmsc436.sheets.Sheets;

public class MainActivity extends AppCompatActivity implements MainContract.View, Sheets.Host {

    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private Toast mToast;
    private Button mPeekButton;
    private BottomSheetBehavior mBottomSheet;
    private ImageView mCloseButton;

    private MainContract.Presenter mPresenter;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mPeekButton = (Button) findViewById(R.id.peeked_begin_button);
        mPeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    mPresenter.onDailyStart();
                }
            }
        });

        mCloseButton = (ImageView) findViewById(R.id.close_bottom_sheet_button);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mPresenter.onCloseBottomSheet();
                }
            }
        });

        findViewById(R.id.expanded_practice_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onCloseBottomSheet();
            }
        });

        mBottomSheet = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mBottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                mPresenter.onBottomSheetStateChange(newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                mPeekButton.setAlpha(1-slideOffset);
                mCloseButton.setAlpha(slideOffset);
                mPresenter.onBottomSheetSlide();
            }
        });

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mRecyclerView = (RecyclerView) findViewById(R.id.practice_test_recyclerview);
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int w = mRecyclerView.getWidth();
                int image_w = (int) getResources().getDimension(R.dimen.practice_icon_size);
                mLayoutManager.setSpanCount(w/((int) (image_w * 1.4)));
            }
        });

        mLayoutManager = new GridLayoutManager(this, 2);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return mLayoutManager.getSpanCount();
                }

                return 1;
            }
        });

        mRecyclerView.setLayoutManager(mLayoutManager);

        mPresenter = new MainPresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public void expandBottomSheet() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBottomSheet.setHideable(false);
                mBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @Override
    public void collapseBottomSheet() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBottomSheet.setHideable(false);
                mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    @Override
    public void hideBottomSheet() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBottomSheet.setHideable(true);
                mBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    @Override
    public void loadActions(final ActionsAdapter adapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
                mRecyclerView.animate()
                        .alpha(0f)
                        .translationY(25)
                        .setDuration(duration)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                mLayoutManager.setSpanSizeLookup(adapter.getSpanLookup(mLayoutManager));
                                mRecyclerView.setAdapter(adapter);
                                mRecyclerView.animate()
                                        .alpha(1.0f)
                                        .translationY(0)
                                        .setDuration(duration)
                                        .start();
                            }
                        }).start();
            }
        });
    }

    @Override
    public void showToast(String message) {
        mToast.setText(message);
        mToast.show();
    }

    @Override
    public void startPracticeMode(String packageName) throws ActivityNotFoundException {
        Intent i = new Intent(packageName + ".action.PRACTICE");
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(i);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showUserSwitcher(final String[] users) {
        final Dialog dialog = new AppCompatDialog(this);

        Arrays.sort(users, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });

        @SuppressLint("InflateParams") // it's fine I swear
        View root = dialog.getLayoutInflater().inflate(R.layout.user_switcher, null, false);
        ListView lv = (ListView) root.findViewById(R.id.users_listview);
        final EditText et = (EditText) root.findViewById(R.id.new_user_edittext);
        Button btn = (Button) root.findViewById(R.id.new_user_create_button);

        lv.setAdapter(new ArrayAdapter<>(this, R.layout.user_switcher_item, R.id.listview_item_textview, users));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                mPresenter.onUserSelected(users[position]);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = et.getText().toString();
                if (user.length() > 0) {
                    dialog.dismiss();
                    mPresenter.onUserCreated(user, UserManager.Handedness.RIGHT, "1/1/1970", UserManager.Gender.MALE);
                }
            }
        });

        dialog.setContentView(root);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public Sheets.Host getHost() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onBackPressed() {
        if (mPresenter.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public int getRequestCode(Sheets.Action action) {
        switch (action) {
            case REQUEST_ACCOUNT_NAME:
                return 436;
            case REQUEST_AUTHORIZATION:
                return 437;
            case REQUEST_CONNECTION_RESOLUTION:
                return 438;
            case REQUEST_PERMISSIONS:
                return 439;
            case REQUEST_PLAY_SERVICES:
                return 440;
            default:
                return 435;
        }
    }

    @Override
    public void notifyFinished(Exception e) {
        // nothing for now
    }

    @Override
    public void onPrescriptionReady(List<String> list) {
        // new prescription for user!
        mPresenter.onPrescriptionReady(list);
    }
}
