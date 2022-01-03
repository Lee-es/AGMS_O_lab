package kr.co.uxn.agms.ui.popup;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import kr.co.uxn.agms.R;

public class ProgressDialogFragment extends DialogFragment {

    public final static String TITLE = "title";
    public final static String MESSAGE = "message";
    public final static String MAX = "max";
    public final static String CANCELABLE = "cancelable";

    public static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.alert_title);
        dialog.setMessage(getString(R.string.progress_dialog_message));
        dialog.setIndeterminate(true);
        Log.e("check","ProgressDialogFragment oncreateDialog");
        return dialog;
    }

}