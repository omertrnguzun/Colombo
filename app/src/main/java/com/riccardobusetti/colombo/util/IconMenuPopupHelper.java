package com.riccardobusetti.colombo.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.appcompat.R;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.ListPopupWindow;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class IconMenuPopupHelper extends MenuPopupHelper implements AdapterView.OnItemClickListener, View.OnKeyListener, ViewTreeObserver.OnGlobalLayoutListener, PopupWindow.OnDismissListener, MenuPresenter {

    private final Context context;
    private final LayoutInflater inflater;
    private final MenuBuilder builder;
    private final MenuAdapter adapter;
    private final boolean isOverflowOnly;
    private final int styleAttr;
    private final int styleRes;

    private View anchorView;
    private ListPopupWindow popupWindow;
    private ViewTreeObserver viewTreeObserver;
    private Callback presenterCallback;

    private boolean hasContentWidth;
    private int contentWidth;
    private int dropDownGravity = Gravity.NO_GRAVITY;

    public IconMenuPopupHelper(Context context, MenuBuilder menu) {
        this(context, menu, null, false, R.attr.popupMenuStyle);
    }

    public IconMenuPopupHelper(Context context, MenuBuilder menu, View anchorView) {
        this(context, menu, anchorView, false, R.attr.popupMenuStyle);
    }

    public IconMenuPopupHelper(Context context, MenuBuilder menu, View anchorView, boolean overflowOnly, int popupStyleAttr) {
        this(context, menu, anchorView, overflowOnly, popupStyleAttr, 0);
    }

    public IconMenuPopupHelper(Context context, MenuBuilder menu, View anchorView, boolean overflowOnly, int popupStyleAttr, int popupStyleRes) {
        super(context, menu, anchorView, overflowOnly, popupStyleAttr, popupStyleRes);

        this.context = context;
        inflater = LayoutInflater.from(context);
        builder = menu;
        adapter = new MenuAdapter(builder);
        isOverflowOnly = overflowOnly;
        styleAttr = popupStyleAttr;
        styleRes = popupStyleRes;
        this.anchorView = anchorView;

        menu.addMenuPresenter(this, context);
    }

    @Override
    public void setAnchorView(View anchor) {
        anchorView = anchor;
    }

    @Override
    public void setGravity(int gravity) {
        dropDownGravity = gravity;
    }

    @Override
    public int getGravity() {
        return dropDownGravity;
    }

    @Override
    public void show() {
        if (!tryShow()) {
            throw new IllegalStateException("MenuPopupHelper cannot be used without an anchor");
        }
    }

    @Override
    public ListPopupWindow getPopup() {
        return popupWindow;
    }

    @Override
    public boolean tryShow() {
        popupWindow = new ListPopupWindow(context, null, styleAttr, styleRes);
        popupWindow.setOnDismissListener(this);
        popupWindow.setOnItemClickListener(this);
        popupWindow.setAdapter(adapter);
        popupWindow.setModal(true);

        View anchor = anchorView;
        if (anchor != null) {
            final boolean addGlobalListener = viewTreeObserver == null;
            viewTreeObserver = anchor.getViewTreeObserver();
            if (addGlobalListener) viewTreeObserver.addOnGlobalLayoutListener(this);
            popupWindow.setAnchorView(anchor);
            popupWindow.setDropDownGravity(dropDownGravity);
        } else {
            return false;
        }

        if (!hasContentWidth) {
            contentWidth = measureContentWidth();
            hasContentWidth = true;
        }

        popupWindow.setContentWidth(contentWidth);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        popupWindow.show();
        popupWindow.getListView().setOnKeyListener(this);
        return true;
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public void onDismiss() {
        popupWindow = null;
        builder.close();
        if (viewTreeObserver != null) {
            if (!viewTreeObserver.isAlive()) viewTreeObserver = anchorView.getViewTreeObserver();
            viewTreeObserver.removeOnGlobalLayoutListener(this);
            viewTreeObserver = null;
        }
    }

    @Override
    public boolean isShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.adapterMenu.performItemAction(adapter.getItem(position), 0);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
            dismiss();
            return true;
        }
        return false;
    }

    private int measureContentWidth() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 168, context.getResources().getDisplayMetrics());
    }

    @Override
    public void onGlobalLayout() {
        if (isShowing()) {
            if (anchorView == null || !anchorView.isShown()) dismiss();
            else popupWindow.show();
        }
    }

    @Override
    public void updateMenuView(boolean cleared) {
        hasContentWidth = false;
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void setCallback(Callback cb) {
        presenterCallback = cb;
    }

    @Override
    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (subMenu.hasVisibleItems()) {
            IconMenuPopupHelper subPopup = new IconMenuPopupHelper(context, subMenu, anchorView);
            subPopup.setCallback(presenterCallback);

            if (subPopup.tryShow()) {
                if (presenterCallback != null) {
                    presenterCallback.onOpenSubMenu(subMenu);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (menu != builder) return;

        dismiss();
        if (presenterCallback != null) {
            presenterCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    private class MenuAdapter extends BaseAdapter {
        private MenuBuilder adapterMenu;
        private int expandedIndex = -1;

        public MenuAdapter(MenuBuilder menu) {
            adapterMenu = menu;
            findExpandedIndex();
        }

        public int getCount() {
            ArrayList<MenuItemImpl> items = isOverflowOnly ? adapterMenu.getNonActionItems() : adapterMenu.getVisibleItems();
            if (expandedIndex < 0) return items.size();
            return items.size() - 1;
        }

        public MenuItemImpl getItem(int position) {
            ArrayList<MenuItemImpl> items = isOverflowOnly ? adapterMenu.getNonActionItems() : adapterMenu.getVisibleItems();
            if (expandedIndex >= 0 && position >= expandedIndex) position++;
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(com.riccardobusetti.colombo.R.layout.item_menu, parent, false);
            }

            final MenuItemImpl menuItem = getItem(position);

            int iconRes = -1;
            try {
                Field field = MenuItemImpl.class.getDeclaredField("mIconResId");
                field.setAccessible(true);
                iconRes = field.getInt(menuItem);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
            if (iconRes == -1) imageView.setImageDrawable(menuItem.getIcon());
            else imageView.setImageDrawable(StaticUtils.getVectorDrawable(context, iconRes));

            if (menuItem.isCheckable() && menuItem.isChecked())
                DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(context, com.riccardobusetti.colombo.R.color.colorAccent));

            TextView textView = (TextView) convertView.findViewById(R.id.title);
            textView.setText(menuItem.getTitle());

            convertView.setTag(position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag();
                    MenuItem item = getItem(position);

                    adapterMenu.performItemAction(item, 0);

                    if (item.isCheckable() && item.isChecked())
                        DrawableCompat.setTint(((ImageView) v.findViewById(R.id.image)).getDrawable(), ContextCompat.getColor(context, com.riccardobusetti.colombo.R.color.colorAccent));
                }
            });

            return convertView;
        }

        void findExpandedIndex() {
            final MenuItemImpl expandedItem = builder.getExpandedItem();
            if (expandedItem != null) {
                final ArrayList<MenuItemImpl> items = builder.getNonActionItems();
                final int count = items.size();
                for (int i = 0; i < count; i++) {
                    final MenuItemImpl item = items.get(i);
                    if (item == expandedItem) {
                        expandedIndex = i;
                        return;
                    }
                }
            }
            expandedIndex = -1;
        }

        @Override
        public void notifyDataSetChanged() {
            findExpandedIndex();
            super.notifyDataSetChanged();
        }
    }
}

