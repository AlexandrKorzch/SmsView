package microsoft.aspnet.signalr.smscodeview.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import microsoft.aspnet.signalr.smscodeview.R;

import static android.view.KeyEvent.KEYCODE_DEL;


public class SmsCodeView extends LinearLayout {

    private int cellCount = 4;
    private int cellLength = 1;
    private int cellTextSize = 45;
    private int cellRightLeftMargin = 12;
    private int cellUnderLineColor = Color.BLACK;
    private int cellUnderLineErrorColor = Color.RED;
    private int cellTextColor = Color.BLACK;
    private int cellCursorColor = Color.BLACK;
    private int cellGravity = Gravity.CENTER_HORIZONTAL;
    private int cellInputType = InputType.TYPE_NULL;
    private boolean notDrawed = true;

    private CellEditText[] edits;
    private InputFilter.LengthFilter mLengthFilter;
    private ColorStateList simpleColorStateList;
    private ColorStateList errorColorStateList;

    public SmsCodeView(Context context) {
        this(context, null);
    }

    public SmsCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmsCodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SmsCodeView, defStyle, 0);
        try {
            cellCount = a.getInteger(R.styleable.SmsCodeView_cellCount, cellCount);
            switch (a.getInteger(R.styleable.SmsCodeView_cellGravity, cellGravity)) {
                case 1:
                    cellGravity = Gravity.CENTER_HORIZONTAL;
                    break;
                case 2:
                    cellGravity = Gravity.START;
                    break;
                case 3:
                    cellGravity = Gravity.END;
            }
            switch (a.getInteger(R.styleable.SmsCodeView_cellInputType, cellInputType)) {
                case 1:
                    cellInputType = InputType.TYPE_CLASS_NUMBER;
                    break;
                case 2:
                    cellInputType = InputType.TYPE_CLASS_TEXT;
            }
            cellTextSize = a.getDimensionPixelSize(R.styleable.SmsCodeView_cellTextSize, cellTextSize);
            cellUnderLineColor = a.getColor(R.styleable.SmsCodeView_cellUnderLineColor, cellUnderLineColor);
            cellUnderLineErrorColor = a.getColor(R.styleable.SmsCodeView_cellUnderLineErrorColor, cellUnderLineErrorColor);
            cellTextColor = a.getColor(R.styleable.SmsCodeView_cellTextColor, cellTextColor);
            cellCursorColor = a.getColor(R.styleable.SmsCodeView_cellCursorColor, cellCursorColor);
            cellRightLeftMargin = a.getDimensionPixelSize(R.styleable.SmsCodeView_cellRightLeftMargin, cellRightLeftMargin);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }

        simpleColorStateList = ColorStateList.valueOf(cellUnderLineColor);
        errorColorStateList = ColorStateList.valueOf(cellUnderLineErrorColor);
        mLengthFilter = new InputFilter.LengthFilter(cellLength);
        edits = new CellEditText[cellCount];
        addEdits();
    }

    @Override
    protected void onLayout(boolean changed, int left, int t, int right, int b) {
        super.onLayout(changed, left, t, right, b);
        int cellWidth = (right - left) / cellCount - cellRightLeftMargin * 2;
        if (notDrawed) drawEdits(cellWidth);
    }

    private void addEdits() {
        for (int i = 0; i < cellCount; i++) {
            CellEditText editText = new CellEditText(getContext(), i);
            editText.setInputType(cellInputType);
            editText.setSupportBackgroundTintList(simpleColorStateList);
            editText.setGravity(cellGravity);
            editText.setTextColor(cellTextColor);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, cellTextSize);
            editText.setCursorColor(cellCursorColor);
            editText.setImeOptions(i == (cellCount - 1) ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
            edits[i] = editText;
        }
    }

    private void drawEdits(int cellWidth) {
        notDrawed = false;
        LayoutParams params = new LayoutParams(cellWidth, LayoutParams.WRAP_CONTENT);
        params.setMargins(cellRightLeftMargin, 0, cellRightLeftMargin, 0);
        for (CellEditText editText : edits) {
            if (editText != null) {
                editText.setLayoutParams(params);
                addView(editText);
            }
        }
    }

    public void showCode(String sms) {
        final char numbers[] = sms.toCharArray();
        if (numbers.length == cellCount) {
            insertSymbols(numbers);
        } else {
            reloadView(numbers);
        }
    }

    public String getCode() {
        StringBuilder builder = new StringBuilder();
        try {
            for (AppCompatEditText editText : edits) {
                builder.append(editText.getText().toString());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public void totalError(boolean error) {
        try {
            ColorStateList colorStateList = error ? errorColorStateList : simpleColorStateList;
            for (AppCompatEditText editText : edits) {
                editText.setSupportBackgroundTintList(colorStateList);
            }
        } catch (NullPointerException e) {
            new Handler().postDelayed(() -> totalError(error), 300);
        }
    }

    public void errorByIndex(boolean error, int index) {
        try {
            edits[index].setSupportBackgroundTintList(error ? errorColorStateList : simpleColorStateList);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void insertSymbols(final char[] numbers) {
        for (int i = 0; i < numbers.length; i++) {
            if (edits[i] != null) {
                edits[i].setText(String.valueOf(numbers[i]));
            }
        }
        if (edits[cellCount - 1] != null) {
            edits[cellCount - 1].setSelection(cellLength);
        }
    }

    private void reloadView(final char[] numbers) {
        removeAllViews();
        notDrawed = true;
        cellCount = numbers.length;
        edits = new CellEditText[cellCount];
        addEdits();
        insertSymbols(numbers);
    }

    private void nextFocus(int index) {
        if (index != cellCount - 1 && edits[index + 1] != null) {
            edits[index + 1].requestFocus();
        }
    }

    private void previousFocus(int index) {
        edits[index - 1].requestFocus();
    }


    private class CellEditText extends AppCompatEditText {

        private int mIndex;

        private CellEditText(Context context, int index) {
            super(context);
            mIndex = index;
            initFilters();
            initFocusListener();
        }

        private void initFocusListener() {
            setOnFocusChangeListener((view, hasFocus) -> {
                if(hasFocus)setSupportBackgroundTintList(simpleColorStateList);
            });
        }

        private void initFilters() {
            InputFilter[] iFilters = new InputFilter[]{
                    mLengthFilter,
                    (source, start, end, dest, dstart, dend) -> {
                        if (source.equals("") && TextUtils.isEmpty(getText().toString())) {
                            nextFocus(mIndex);
                        } else if (!TextUtils.isEmpty(getText().toString())) {
                            return "";
                        } else {
                            setSupportBackgroundTintList(simpleColorStateList);
                            nextFocus(mIndex);
                        }
                        return source;
                    }
            };
            setFilters(iFilters);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (mIndex != 0 && this.getText().length() == 0
                    && keyCode == KEYCODE_DEL && edits[mIndex - 1] != null) {
                previousFocus(mIndex);
            }
            return super.onKeyDown(keyCode, event);
        }

        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            setSelection(getText().length());
        }

        private void setCursorColor(@ColorInt int color) {
            try {
                Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
                field.setAccessible(true);
                int drawableResId = field.getInt(this);
                field = TextView.class.getDeclaredField("mEditor");
                field.setAccessible(true);
                Object editor = field.get(this);
                Drawable drawable = ContextCompat.getDrawable(this.getContext(), drawableResId);
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                Drawable[] drawables = {drawable, drawable};
                field = editor.getClass().getDeclaredField("mCursorDrawable");
                field.setAccessible(true);
                field.set(editor, drawables);
            } catch (Exception ignored) {
                Log.e("error", "setCursorColor: ignored - " + ignored.getMessage());
            }
        }
    }

    private static final String TAG = "SmsCodeView";
}
