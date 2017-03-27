package se.juneday.throwaway.cavesandmore;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import java.util.List;

import se.itu.game.cave.Player;
import se.itu.game.cave.Room;
import se.itu.game.cave.Thing;
import se.itu.game.test.TestUtils;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Player player;
    private ListView thingsView;
    private ListView inventoryView;

    private ArrayAdapter<Thing> thingsAdapater;
    private ArrayAdapter<Thing> inventoryAdapater;
    private List<Thing> things;
    private List<Thing> inventory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Room startingRoom = TestUtils.getTest1Cave();
        player = Player.getInstance(startingRoom);
        Log.d(LOG_TAG, "player: " + player);

        inventory = player.inventory();
        Log.d(LOG_TAG, "inventory: " + inventory);
        inventoryView = (ListView) findViewById(R.id.inventory);
        inventoryAdapater = new ArrayAdapter<Thing>(this,
                android.R.layout.simple_list_item_1, inventory);
        inventoryView.setAdapter(inventoryAdapater);
        TextView inventoryHeader = new TextView(this);
        inventoryHeader.setText("Player items");
        inventoryView.addHeaderView(inventoryHeader, null, false);

        things = player.currentRoom().things();
        Log.d(LOG_TAG, "things: " + things);
        thingsView = (ListView) findViewById(R.id.things);
        thingsAdapater = new ArrayAdapter<Thing>(this,
                android.R.layout.simple_list_item_1, things);
        thingsView.setAdapter(thingsAdapater);
        TextView thingsHeader = new TextView(this);
        thingsHeader.setText("Room items");
        thingsView.addHeaderView(thingsHeader, null, false);

        inventoryView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG,
                        "item clicked, pos:" + i + " l: " + l + " " + inventory.get((int)l));
                player.dropThing(inventory.get((int)l));
                inventoryView.invalidateViews();
                thingsView.invalidateViews();
            }
        });
        thingsView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG,
                        "item clicked, pos:" + i + " l: " + l );
                Log.d(LOG_TAG,
                        "item clicked, pos:" + i + " l: " + l + " " + player.currentRoom().things().get((int)l));
                player.takeThing(things.get((int)l));
                inventoryView.invalidateViews();
                thingsView.invalidateViews();
            }
        });
    }

    private void updateImageButton(int buttonId, Room.Direction direction) {
        ImageButton ib = ((ImageButton)findViewById(buttonId));
        boolean state = player.currentRoom().getRoom(direction)!=null;
        ib.setClickable(state);
        Log.d(LOG_TAG, " room:  " + player.currentRoom());
        Log.d(LOG_TAG, " imagebutton for " + direction + " (" + state + "): " + player.currentRoom().getRoom(direction) );
        if (state) {
            ib.getBackground().setAlpha(255);          ;
        } else {
            ib.getBackground().setAlpha(32);          ;
        }
    }

    private void updateDescription(String description) {
        inventory = player.inventory();
        things = player.currentRoom().things();
        Log.d(LOG_TAG, "    -------------------------");
        Log.d(LOG_TAG, "    player: " + player);
        Log.d(LOG_TAG, "    inventory: " + inventory);
        Log.d(LOG_TAG, "    things: " + things);
        Log.d(LOG_TAG, "    room: " + player.currentRoom());
        Log.d(LOG_TAG, "    -------------------------");

        updateImageButton(R.id.north_button, Room.Direction.NORTH);
        updateImageButton(R.id.east_button, Room.Direction.EAST);
        updateImageButton(R.id.west_button, Room.Direction.WEST);
        updateImageButton(R.id.south_button, Room.Direction.SOUTH);

        TextView tv = (TextView) findViewById(R.id.description);
        tv.setText(description);

/*        inventoryView.invalidateViews();
        thingsView.invalidateViews();
*/

        thingsView.setAdapter(null);
        thingsAdapater = new ArrayAdapter<Thing>(this,
                android.R.layout.simple_list_item_1, things);
        thingsView.setAdapter(thingsAdapater);

        thingsAdapater.notifyDataSetChanged();
        inventoryAdapater.notifyDataSetChanged();

        inventoryView.invalidateViews();
        thingsView.invalidateViews();

    }

    public void goDir(Room.Direction dir) {
        Log.d(LOG_TAG, "--> goDir " + dir);
        Log.d(LOG_TAG, "    player: " + player);
        player.go(dir);
        Log.d(LOG_TAG, "    player: " + player);
        updateDescription(player.currentRoom().description());
        Log.d(LOG_TAG, "<-- goDir " + dir);
    }

    public void goWest(View v) {
        goDir(Room.Direction.WEST);
    }
    public void goEast(View v) {
        goDir(Room.Direction.EAST);
    }
    public void goNorth(View v) {
        goDir(Room.Direction.NORTH);
    }
    public void goSouth(View v) {
        goDir(Room.Direction.SOUTH);
    }
}
