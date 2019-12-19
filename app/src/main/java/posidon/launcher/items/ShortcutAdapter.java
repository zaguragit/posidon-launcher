package posidon.launcher.items;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import posidon.launcher.R;

@RequiresApi(api = Build.VERSION_CODES.N_MR1)
public class ShortcutAdapter extends RecyclerView.Adapter<ShortcutAdapter.ShortcutViewHolder> {

    private final Context context;
    private final List<ShortcutInfo> shortcuts;
    private final int txtcolor;

    public ShortcutAdapter(Context context, List<ShortcutInfo> shortcuts, int txtcolor) {
        this.context = context;
        this.shortcuts = shortcuts;
        this.txtcolor = txtcolor;
    }

    @NonNull
    @Override
    public ShortcutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shortcut, parent, false);
        return new ShortcutViewHolder(v);
    }

    static class ShortcutViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        ShortcutViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ShortcutViewHolder holder, final int position) {
        TextView txt = holder.view.findViewById(R.id.icontxt);
        txt.setText(shortcuts.get(position).getShortLabel());
        txt.setTextColor(txtcolor);

        ((ImageView)holder.view.findViewById(R.id.iconimg)).setImageDrawable(((LauncherApps)(Objects.requireNonNull(context.getSystemService(Context.LAUNCHER_APPS_SERVICE)))).getShortcutIconDrawable(shortcuts.get(position), context.getResources().getDisplayMetrics().densityDpi));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemLongPress.currentPopup.dismiss();
                ((LauncherApps)(Objects.requireNonNull(context.getSystemService(Context.LAUNCHER_APPS_SERVICE)))).startShortcut(shortcuts.get(position), null, ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.slightfadeout).toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return shortcuts.size();
    }
}
