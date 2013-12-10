package app.game.tictactoe.util;

import java.lang.reflect.InvocationTargetException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AlertDialogFragment extends DialogFragment {
	public static AlertDialogFragment newInstance(final Bundle dialogBundle) {
		final AlertDialogFragment frag = new AlertDialogFragment();
		frag.setArguments(dialogBundle);

		return frag;
	}

	@SuppressWarnings("all")
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Activity activity = this.getActivity();

		return new AlertDialog.Builder(this.getActivity())
		.setPositiveButton(this.getArguments().getCharSequence("positive"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				try {
					activity.getClass().getMethod("doPositiveClick", null).invoke(activity, null);
				} catch (final NoSuchMethodException e) {
					Toast.makeText(activity, "Class does not implement doPositiveClick", Toast.LENGTH_SHORT).show();
				} catch (final IllegalArgumentException e) {
					Toast.makeText(activity, "Illegal arguments on doPositiveClick", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					Toast.makeText(activity, "Illegal access on doPositiveClick", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				} catch (final InvocationTargetException e) {
					Toast.makeText(activity, "Invocation target exception on doPositiveClick", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		}).setNegativeButton(this.getArguments().getCharSequence("negative"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				try {
					activity.getClass().getMethod("doNegativeClick", null).invoke(activity, null);
				} catch (final NoSuchMethodException e) {
					Toast.makeText(activity, "Class does not implement doPositiveClick", Toast.LENGTH_SHORT).show();
				} catch (final IllegalArgumentException e) {
					Toast.makeText(activity, "Illegal arguments on doPositiveClick", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					Toast.makeText(activity, "Illegal access on doPositiveClick", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				} catch (final InvocationTargetException e) {
					Toast.makeText(activity, "Invocation target exception on doPositiveClick", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		}).setMessage(this.getArguments().getCharSequence("state")).setTitle(this.getArguments().getCharSequence("title")).create();
	};
}
