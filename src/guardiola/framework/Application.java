package guardiola.framework;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public final class Application {

    private final ArrayList<Action> actions = new ArrayList<>();;

    public Application() {
        init();
    }

    private void init() {
        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            Properties properties = new Properties();
            properties.load(is);
            scan(properties.getProperty("fwg.actions"));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo de configuración.", e);
        }
    }

    private void scan(String actionsClassPath) {
        try {
            for (Class<? extends Action> actionClass : new Reflections(actionsClassPath).getSubTypesOf(Action.class)) {
                actions.add(
                        actionClass.getDeclaredConstructor().newInstance()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron cargar las Action Class.", e);
        }
    }

    public void start() {

        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;

        try {

            screen = terminalFactory.createScreen();
            screen.startScreen();

            final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

            final Window window = new BasicWindow("Trabajo Práctico N°8 \"Frameworks\" | Guardiola, Lucas Joel");

            GridLayout gridLayout = new GridLayout(2);
            Panel contentPanel = new Panel(gridLayout);
            gridLayout.setHorizontalSpacing(3);

            contentPanel.addComponent(new Label("Elige una de las acciones:"));

            ComboBox<Action> actionComboBox = new ComboBox<>(actions)
                    .setReadOnly(true)
                    .setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
            contentPanel.addComponent(actionComboBox);

            contentPanel.addComponent(
                    new Separator(Direction.HORIZONTAL)
                            .setLayoutData(
                                    GridLayout.createHorizontallyFilledLayoutData(2)));

            contentPanel.addComponent(
                    new Button("Ejecutar",
                            () -> {
                                Action selectedItem = actionComboBox.getSelectedItem();
                                selectedItem.run();
                                MessageDialog.showMessageDialog(
                                        textGUI, "Éxito", "Se ejecuto \"" + selectedItem + "\" con éxito", MessageDialogButton.OK
                                );
                            }
                    ).setLayoutData(
                            GridLayout.createHorizontallyEndAlignedLayoutData(1)
                    )
            );

            contentPanel.addComponent(
                    new Button("Salir", window::close).setLayoutData(
                            GridLayout.createHorizontallyEndAlignedLayoutData(2)));

            window.setComponent(contentPanel);

            textGUI.addWindowAndWait(window);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(screen != null) {
                try {
                    screen.stopScreen();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
