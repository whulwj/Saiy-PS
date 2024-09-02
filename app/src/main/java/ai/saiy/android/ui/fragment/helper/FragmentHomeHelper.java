/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.ProductDetails;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.firebase.database.reference.IAPCodeReference;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentHomeHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentHomeHelper.class.getSimpleName();

    private final FragmentHome parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentHomeHelper(@NonNull final FragmentHome parentFragment) {
        this.parentFragment = parentFragment;
    }

    /**
     * Get the components for this fragment
     *
     * @return a list of {@link ContainerUI} elements
     */
    private ArrayList<ContainerUI> getUIComponents() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUIComponents");
        }

        final ArrayList<ContainerUI> mObjects = new ArrayList<>();

        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_voice_tutorial));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_begin));
        containerUI.setIconMain(R.drawable.ic_text_to_speech);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_user_guide));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_topics));
        containerUI.setIconMain(R.drawable.ic_library);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_command_list));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_command_list);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_development));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_contribute));
        containerUI.setIconMain(R.drawable.ic_pulse);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.title_settings));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_cog);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.title_customisation));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_tweak));
        containerUI.setIconMain(R.drawable.ic_fingerprint);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.title_advanced));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_pill);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.title_troubleshooting_bugs));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_help));
        containerUI.setIconMain(R.drawable.ic_bug);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_donate));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_contribute));
        containerUI.setIconMain(R.drawable.ic_gift);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        return mObjects;
    }

    /**
     * Get the recycler view for this fragment
     *
     * @param parent the view parent
     * @return the {@link RecyclerView}
     */
    public RecyclerView getRecyclerView(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }

        final RecyclerView mRecyclerView = (RecyclerView)
                parent.findViewById(R.id.layout_common_fragment_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getParentActivity(), null));

        return mRecyclerView;
    }

    /**
     * Get the adapter for this fragment
     *
     * @param mObjects list of {@link ContainerUI} elements
     * @return the {@link UIMainAdapter}
     */
    public UIMainAdapter getAdapter(@NonNull final ArrayList<ContainerUI> mObjects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIMainAdapter(mObjects, getParent(), getParent());
    }

    /**
     * Update the parent fragment with the UI components. If the drawer is not open in the parent
     * Activity, we can assume this method is called as a result of the back button being pressed, or
     * the first initialisation of the application - neither of which require a delay.
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentHomeHelper.this.getUIComponents();

                if (FragmentHomeHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentHomeHelper.this.getParent().isActive()) {

                    FragmentHomeHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FragmentHomeHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentHomeHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentHomeHelper.this.getParent().getObjects().size());
                        }
                    });

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        });
    }

    public void showPremiumDialog(List<com.android.billingclient.api.ProductDetails> productDetailsList) {
        showProgress(false);
        final View view = LayoutInflater.from(getParentActivity()).inflate(R.layout.premium_dialog_layout, null);
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setTitle(R.string.menu_donate)
                .setIcon(R.drawable.ic_gift)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showPremiumDialog: onNegative");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showPremiumDialog: onCancel");
                        }
                    }
                }).create();

        com.android.billingclient.api.ProductDetails productDetailsStarter = null, productDetailsBronze = null, productDetailsSilver = null, productDetailsGold = null, productDetailsPlatinum = null;
        try {
            String productId;
            for (com.android.billingclient.api.ProductDetails productDetails : productDetailsList) {
                productId = productDetails.getProductId();
                if (UserFirebaseHelper.LEVEL_1.matches(productId)) {
                    productDetailsStarter = productDetails;
                } else if (UserFirebaseHelper.LEVEL_2.matches(productId)) {
                    productDetailsBronze = productDetails;
                } else if (UserFirebaseHelper.LEVEL_3.matches(productId)) {
                    productDetailsSilver = productDetails;
                } else if (UserFirebaseHelper.LEVEL_4.matches(productId)) {
                    productDetailsGold = productDetails;
                } else if (UserFirebaseHelper.LEVEL_5.matches(productId)) {
                    productDetailsPlatinum = productDetails;
                }
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "showPremiumDialog: NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "showPremiumDialog: Exception");
                e.printStackTrace();
            }
        }

        LinearLayout linearLayout = view.findViewById(R.id.linPlatinum);
        if (productDetailsPlatinum != null) {
            final TextView textViewPlatinum = linearLayout.findViewById(R.id.tvPlatinumCost);
            final com.android.billingclient.api.ProductDetails finalProductDetailsPlatinum = productDetailsPlatinum;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "showPremiumDialog: LEVEL_5: onClick");
                    }
                    materialDialog.dismiss();
                    getParentActivity().startPurchaseFlow(finalProductDetailsPlatinum);
                }
            });
            showPrice(productDetailsPlatinum, textViewPlatinum, null);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
        linearLayout = view.findViewById(R.id.linGold);
        if (productDetailsGold != null) {
            final TextView textViewGold = linearLayout.findViewById(R.id.tvGoldCost);
            final com.android.billingclient.api.ProductDetails finalProductDetailsGold = productDetailsGold;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "showPremiumDialog: LEVEL_4: onClick");
                    }
                    materialDialog.dismiss();
                    getParentActivity().startPurchaseFlow(finalProductDetailsGold);
                }
            });
            showPrice(productDetailsGold, textViewGold, null);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
        linearLayout = view.findViewById(R.id.linSilver);
        if (productDetailsSilver != null) {
            final TextView textViewSilver = linearLayout.findViewById(R.id.tvSilverCost);
            final com.android.billingclient.api.ProductDetails finalProductDetailsSilver = productDetailsSilver;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "showPremiumDialog: LEVEL_3: onClick");
                    }
                    materialDialog.dismiss();
                    getParentActivity().startPurchaseFlow(finalProductDetailsSilver);
                }
            });
            showPrice(productDetailsSilver, textViewSilver, null);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
        linearLayout = view.findViewById(R.id.linBronze);
        if (productDetailsBronze != null) {
            final TextView textViewBronze = linearLayout.findViewById(R.id.tvBronzeCost);
            final com.android.billingclient.api.ProductDetails finalProductDetailsBronze = productDetailsBronze;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "showPremiumDialog: LEVEL_2: onClick");
                    }
                    materialDialog.dismiss();
                    getParentActivity().startPurchaseFlow(finalProductDetailsBronze);
                }
            });
            showPrice(productDetailsBronze, textViewBronze, null);
        } else {
            linearLayout.setVisibility(View.GONE);
        }

        linearLayout = view.findViewById(R.id.linAdvert);
        final TextView textViewAdvert = linearLayout.findViewById(R.id.tvAdvertCost);
        textViewAdvert.setText(R.string.iap_no_cost);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "showPremiumDialog: Ad onClick");
                }
                materialDialog.dismiss();
                if (ai.saiy.android.utils.SPH.getAdvertisementOverview(getApplicationContext())) {
                    showAd();
                } else {
                    ai.saiy.android.utils.SPH.markAdvertisementOverview(getApplicationContext());
                    showAdOverviewDialog();
                }
            }
        });
        linearLayout = view.findViewById(R.id.linStarter);
        if (productDetailsStarter != null) {
            final TextView textViewStarter = linearLayout.findViewById(R.id.tvStarterCost);
            final com.android.billingclient.api.ProductDetails finalProductDetailsStarter = productDetailsStarter;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "showPremiumDialog: LEVEL_1: onClick");
                    }
                    materialDialog.dismiss();
                    getParentActivity().startPurchaseFlow(finalProductDetailsStarter);
                }
            });
            showPrice(productDetailsStarter, textViewStarter, textViewAdvert);
        } else {
            linearLayout.setVisibility(View.GONE);
        }

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    private void showPrice(@NonNull ProductDetails productDetails, @NonNull TextView textViewPrice, @Nullable TextView textViewAdvert) {
        final com.android.billingclient.api.ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails = productDetails.getOneTimePurchaseOfferDetails();
        if (oneTimePurchaseOfferDetails != null) {
            textViewPrice.setText(oneTimePurchaseOfferDetails.getFormattedPrice());
            if (textViewAdvert != null) {
                textViewAdvert.setText(oneTimePurchaseOfferDetails.getPriceCurrencyCode() + getApplicationContext().getString(R.string.iap_no_cost));
            }
            return;
        }
        final List<com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
        if (subscriptionOfferDetails == null) {
            return;
        }
        final List<ProductDetails.PricingPhase> pricingPhases = new ArrayList<>();
        for (ProductDetails.SubscriptionOfferDetails subscriptionOffer : subscriptionOfferDetails) {
            pricingPhases.addAll(subscriptionOffer.getPricingPhases().getPricingPhaseList());
        }
        if (pricingPhases.isEmpty()) {
            return;
        }
        pricingPhases.sort(new Comparator<ProductDetails.PricingPhase>() {
            @Override
            public int compare(ProductDetails.PricingPhase pricingPhase1, ProductDetails.PricingPhase pricingPhase2) {
                return Long.compare(pricingPhase1.getPriceAmountMicros(), pricingPhase2.getPriceAmountMicros());
            }
        });
        final ProductDetails.PricingPhase pricingPhase = pricingPhases.get(0);
        textViewPrice.setText(pricingPhase.getFormattedPrice());
        if (textViewAdvert != null) {
            textViewAdvert.setText(pricingPhase.getPriceCurrencyCode() + getApplicationContext().getString(R.string.iap_no_cost));
        }
    }

    public void showAccountPicker() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Pair<Boolean, String> iapKeyPair = new IAPCodeReference().getKey(getApplicationContext());
                if (!iapKeyPair.first) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "showAccountPicker unable to fetch iapKey");
                    }
                    if (getParent().isActive()) {
                        showProgress(false);
                        getParent().toast(getApplicationContext().getString(R.string.network_error), Toast.LENGTH_SHORT);
                    }
                    return;
                }
                getParent().getBillingViewModel().setIapKey(iapKeyPair.second);
                if (getParent().isActive()) {
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getParent().startActivityForResult(AccountPicker.newChooseAccountIntent(new AccountPicker.AccountChooserOptions.Builder().setAllowableAccountsTypes(Collections.singletonList(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)).build()), FragmentHome.ACCOUNT_PICKER_REQUEST_CODE);
                        }
                    });
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "showAccountPicker Fragment detached");
                }
            }
        }).start();
    }

    private void showAdOverviewDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showAdOverviewDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_donate)
                .setMessage(R.string.content_ad_overview)
                .setIcon(R.drawable.ic_gift)
                .setNeutralButton(R.string.title_watch_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAdOverviewDialog: onNeutral");
                        }
                        showAd();
                    }
                })
                .setNegativeButton(R.string.title_maybe_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAdOverviewDialog: onNegative");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAdOverviewDialog: onCancel");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    private void showAd() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showAd");
        }
        if (getParent().isActive()) {
            getParentActivity().showReward();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "showAd: finishing");
        }
    }

    public void showUserGuideDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_user_guide)
                .setIcon(R.drawable.ic_library)
        .setItems(getParentActivity().getResources().getStringArray(R.array.array_user_guide), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Global.isInVoiceTutorial()) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onClick: tutorialActive");
                            }
                            getParent().toast(getParent().getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
                            return;
                        }

                        switch (which) {
                            case 0:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_BASIC");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_GUIDE_BASIC);
                                break;
                            case 1:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_CUSTOM_COMMANDS");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_CUSTOM_COMMANDS);
                                break;
                            case 2:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_CUSTOM_REPLACEMENTS");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_CUSTOM_REPLACEMENTS);
                                break;
                            case 3:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_SOUND_EFFECTS");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_SOUND_EFFECTS);
                                break;
                            case 4:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_TASKER");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_TASKER);
                                break;
                            case 5:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_TROUBLESHOOTING");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_TROUBLESHOOTING);
                                break;
                            case 6:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_COMING_SOON");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_COMING_SOON);
                                break;
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onNegative");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    /**
     * Utility method to ensure we double check the context being used.
     *
     * @return the application context
     */
    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    public void showProgress(boolean visible) {
        if (getParent().isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    /**
     * Utility to return the parent activity neatly cast. No need for instanceOf as this
     * fragment helper will never be attached to another activity.
     *
     * @return the {@link ActivityHome} parent
     */

    public ActivityHome getParentActivity() {
        return parentFragment.getParentActivity();
    }

    /**
     * Utility method to return the parent fragment this helper is helping.
     *
     * @return the parent fragment
     */
    public FragmentHome getParent() {
        return parentFragment;
    }
}
