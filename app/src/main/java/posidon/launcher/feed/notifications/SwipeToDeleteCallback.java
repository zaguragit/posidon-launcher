package posidon.launcher.feed.notifications;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    public static SwipeListener swipeListener;

    public SwipeToDeleteCallback() { super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT); }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

    @Override
    public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int direction) { swipeListener.onSwipe(viewHolder, direction); }

    public interface SwipeListener { void onSwipe(RecyclerView.ViewHolder viewHolder, int direction); }
}