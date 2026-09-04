package cm.ndicsonlabs.bossapp.ui.ux.spreadsheet;

import com.vaadin.collaborationengine.CollaborationEngine;
import com.vaadin.collaborationengine.CollaborationMap;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.*;
import com.vaadin.flow.shared.Registration;

import java.util.concurrent.CompletableFuture;

@Tag("collaborative-jspreadsheet")
public class CollaborativeJSpreadsheetkit extends JSpreadsheet {

    private final String topicId;
    private final UserInfo localUser;
    private boolean isApplyingRemoteChange = false;

    public CollaborativeJSpreadsheetkit(String topicId, UserInfo localUser) {
        this.topicId = topicId;
        this.localUser = localUser;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Open a connection to the Collaboration Engine Topic
        CollaborationEngine.getInstance().openTopicConnection(this, topicId, localUser, topic -> {
            CollaborationMap cellValuesMap = topic.getNamedMap("cellValues");

            // 1. Listen to LOCAL cell changes and publish them to the Topic Map
            Registration cellChangeRegistration = addCellChangeListener(event -> {
                if (isApplyingRemoteChange) return; // Prevent echo loops
                
                String key = event.getWorksheetIndex() + "-" + event.getX() + "-" + event.getY();
                String jsonValue = JSpreadsheetJson.toJson(event.getValue());
                cellValuesMap.put(key, jsonValue);
            });

            // 2. Subscribe to Topic Map changes and apply them to the LOCAL JS component
            Registration mapSubscription = cellValuesMap.subscribe(event -> {
                String key = event.getKey();
                String[] parts = key.split("-");
                if (parts.length == 3) {
                    int ws = Integer.parseInt(parts[0]);
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    
                    String jsonValue = event.getValue(String.class);
                    Object value = JSpreadsheetJson.fromJson(jsonValue, Object.class);

                    isApplyingRemoteChange = true;
                    applyRemoteChange(ws, x, y, value).whenComplete((v, ex) -> isApplyingRemoteChange = false);
                }
            });

            return Registration.combine(cellChangeRegistration, mapSubscription);
        });
    }

    public CompletableFuture<Void> applyRemoteChange(int worksheetIndex, int x, int y, Object value) {
        return getElement().executeJs("""
            if (this._jspreadsheetConnector) {
                this._jspreadsheetConnector.applyRemoteChange($0, $1, $2, JSON.parse($3));
            }
            """, worksheetIndex, x, y, JSpreadsheetJson.toJson(value))
            .toCompletableFuture(Void.class);
    }

    // This event must be present in your component hierarchy to capture JS DOM events
    @DomEvent("jspreadsheet-cell-changed")
    public static class CellChangeEvent extends com.vaadin.flow.component.ComponentEvent<JSpreadsheet> {
        private final int worksheetIndex, x, y;
        private final Object value;

        public CellChangeEvent(JSpreadsheet source, boolean fromClient,
                               @EventData("event.detail.worksheetIndex") int worksheetIndex,
                               @EventData("event.detail.x") int x,
                               @EventData("event.detail.y") int y,
                               @EventData("event.detail.value") Object value) {
            super(source, fromClient);
            this.worksheetIndex = worksheetIndex;
            this.x = x; this.y = y; this.value = value;
        }
        public int getWorksheetIndex() { return worksheetIndex; }
        public int getX() { return x; }
        public int getY() { return y; }
        public Object getValue() { return value; }
    }

    public Registration addCellChangeListener(ComponentEventListener<CellChangeEvent> listener) {
        return addListener(CellChangeEvent.class, listener);
    }
}