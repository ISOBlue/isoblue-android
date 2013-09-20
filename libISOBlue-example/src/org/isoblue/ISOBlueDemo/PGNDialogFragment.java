package org.isoblue.ISOBlueDemo;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;

public class PGNDialogFragment extends DialogFragment {

	private TypedArray PGNVals;
	private ArrayList<Integer> mSelectedPGNs;
	
	public ArrayList<Integer> getPGNs() {
		return mSelectedPGNs;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		
		PGNVals.recycle();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		PGNVals = getResources().obtainTypedArray(R.array.pgn_vals);
		mSelectedPGNs = getArguments().getIntegerArrayList("pgns");
		
		boolean checked[] = new boolean[PGNVals.length()];
		for(int i = 0; i < PGNVals.length(); i++) {
			checked[i] = mSelectedPGNs.contains(PGNVals.getInteger(i, -1));
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Set the dialog title
		builder.setTitle(R.string.select_pgns)
		// Specify the list array, the items to be selected by default
		// (null for none),
		// and the listener through which to receive callbacks when
		// items are selected
				.setMultiChoiceItems(R.array.pgn_names, checked,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								Integer val = PGNVals.getInteger(which, -1);
								if (isChecked) {
									// If the user checked the item, add it to
									// the selected items
									mSelectedPGNs.add(val);
								} else if (mSelectedPGNs.contains(val)) {
									// Else, if the item is already in the
									// array, remove it
									mSelectedPGNs.remove(Integer.valueOf(val));
								}
							}
						});

		return builder.create();
	}
}
