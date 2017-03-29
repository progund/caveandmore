package se.juneday.throwaway.cavesandmore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import se.itu.game.cave.Player;
import se.itu.game.cave.Room;
import se.itu.game.cave.Thing;
import se.itu.game.cave.init.CaveInitializer;
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

    private final static String CAVE_DB = "/data/data/se.juneday.throwaway.cavesandmore/cave.db";
    private final static String ASSET_DB = "cavedatabas.db";

    enum ClickType { TAKE, DROP ; }

    private void asstDbToFile() throws IOException {
        if (new File(CAVE_DB).isFile()) {
            Log.d(LOG_TAG, "Not extracting db from asset");
            return;
        }
        final InputStream inputStream = getAssets().open(ASSET_DB);
        final OutputStream fileStream = new FileOutputStream(CAVE_DB);

        final byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            fileStream.write(buffer, 0, length);
        }

        fileStream.flush();
        fileStream.close();
        inputStream.close();
    }
    
    private void setListViewHeader(ListView lv, String header) {
        TextView inventoryHeader = new TextView(this);
        inventoryHeader.setText(header);
        lv.addHeaderView(inventoryHeader, null, false);
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.show();
        Log.d(LOG_TAG, "showToast: " + msg);
    }

    private void setupGame() {
        try {
            asstDbToFile();
            CaveInitializer init;
            init = CaveInitializer.getInstance();
            init.initAll();
            player = Player.getInstance(init.getFirstRoom());
            showToast("Full version");
        } catch (Exception e) {
            Room startingRoom = TestUtils.getTest1Cave();
            player = Player.getInstance(startingRoom);
            showToast("Test version");
        }
        inventory = player.inventory();
        things = player.currentRoom().things();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupGame();

        inventoryView = (ListView) findViewById(R.id.inventory);
        inventoryAdapater = resetAdapter(inventoryView, inventory);
        setListViewHeader(inventoryView, "Player items");

        thingsView = (ListView) findViewById(R.id.things);
        thingsAdapater = resetAdapter(thingsView,things);
        setListViewHeader(thingsView, "Room items");

        // set click listeners for inventory and room item list
        inventoryView.setOnItemClickListener( (adapterView, view, i, l) -> { 
                handleThingClick(ClickType.DROP, (int) l);
        });
        thingsView.setOnItemClickListener( (adapterView, view, i, l) -> {
                handleThingClick(ClickType.TAKE, (int) l);
        });
    }

    private void handleThingClick(ClickType ct , int itemIndex) {
        switch (ct) {
        case TAKE:
            player.takeThing(things.get(itemIndex));
            break;
        case DROP:
            player.dropThing(inventory.get(itemIndex));
            break;
        default:
            throw new IllegalArgumentException("Unknown ClikcType: " + ct);
        }
        inventoryView.invalidateViews();
        thingsView.invalidateViews();
    }
    
    private void updateImageButton(int buttonId, Room.Direction direction) {
        ImageButton ib = ((ImageButton)findViewById(buttonId));
        boolean state = player.currentRoom().getRoom(direction)!=null;
        ib.getBackground().setAlpha(state ? 255 : 32);
        ib.setClickable(state);        
    }

    private void updateGui() {
        String description = player.currentRoom().description();
        inventory = player.inventory();
        things = player.currentRoom().things();
        Log.d(LOG_TAG, "    /------------------------");
        Log.d(LOG_TAG, "    | player: " + player);
        Log.d(LOG_TAG, "    | inventory: " + inventory);
        Log.d(LOG_TAG, "    | things: " + things);
        Log.d(LOG_TAG, "    | room: " + player.currentRoom());
        Log.d(LOG_TAG, "    \\------------------------");

        updateImageButton(R.id.north_button, Room.Direction.NORTH);
        updateImageButton(R.id.east_button, Room.Direction.EAST);
        updateImageButton(R.id.west_button, Room.Direction.WEST);
        updateImageButton(R.id.south_button, Room.Direction.SOUTH);

        TextView tv = (TextView) findViewById(R.id.description);
        tv.setText(description);

        thingsAdapater = resetAdapter(thingsView,things);
    }

    private ArrayAdapter<Thing> resetAdapter(ListView lv, List<Thing> things) {
        lv.setAdapter(null);
        ArrayAdapter<Thing> adapter = new ArrayAdapter<Thing>(this,
                        android.R.layout.simple_list_item_1, things);
        lv.setAdapter(adapter);
        lv.invalidateViews();
        return adapter;
    }

    public void goDir(Room.Direction dir) {
        player.go(dir);
        updateGui();
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
