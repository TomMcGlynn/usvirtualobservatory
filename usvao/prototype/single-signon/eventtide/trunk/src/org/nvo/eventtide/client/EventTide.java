package org.nvo.eventtide.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.uiuc.ncsa.adminkit.client.util.widget.TopBar;
import org.nvo.eventtide.client.orm.Detail;

import java.util.ArrayList;

public class EventTide implements EntryPoint {
    private static final String[] phases = { "predict", "begin", "fail", "succeed", "resolve" };

    private int phase;

    public void onModuleLoad() {
        DockLayoutPanel appPanel = new DockLayoutPanel(Style.Unit.EM);
        RootLayoutPanel.get().add(appPanel);

        // top: title, status, login
        TopBar top = new TopBar();
        top.setStyleName("top");
        appPanel.addNorth(top, 3);

        // left: ...
        VerticalPanel navPanel = new VerticalPanel();
        appPanel.add(navPanel);
        final TextBox activity = new TextBox();
        activity.setText("dawn");
        navPanel.add(activity);
        final Label phaseLabel = new Label(phases[phase]);
        navPanel.add(phaseLabel);
        final Label statusLabel = new Label();
        final Button doButton = new Button("Do", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ArrayList<Detail> details = new ArrayList<Detail>();
                details.add(new Detail("foo", "bar"));
                details.add(new Detail("baz", "qux"));
                LogService.App.getInstance().doSomething(activity.getText(), phases[phase], details, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        statusLabel.setText("Failed: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        statusLabel.setText("Succeeded!");
                        ++phase;
                        if (phase >= phases.length) {
                            phase = 0;
                            activity.setText("");
                            activity.setFocus(true);
                        }
                        phaseLabel.setText(phases[phase]);
                    }
                });
            }
        });
        navPanel.add(doButton);
        activity.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                doButton.setEnabled(activity.getText().trim().length() > 0);
            }
        });
        navPanel.add(statusLabel);
    }
}
