package app;

import controls.InputFactory;
import dialogs.PanelInfo;
import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Surface;
import misc.CoordinateSystem2i;
import panels.PanelGame;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

import static app.Colors.*;

/**
 * Класс окна приложения
 */
public class Application implements Consumer<Event> {
    /**
     * Режимы работы приложения
     */
    public enum Mode {
        /**
         * Основной режим работы
         */
        WORK,
        /**
         * Окно информации
         */
        INFO
    }

    /**
     * окно приложения
     */
    private final Window window;
    /**
     * отступ приложения
     */
    public static final int PANEL_PADDING = 5;
    /**
     * радиус скругления элементов
     */
    public static final int C_RAD_IN_PX = 4;
    /**
     * кнопка изменений: у мака - это `Command`, у windows - `Ctrl`
     */
    public static final KeyModifier MODIFIER = Platform.CURRENT == Platform.MACOS ? KeyModifier.MAC_COMMAND : KeyModifier.CONTROL;
    /**
     * время последнего нажатия клавиши мыши
     */
    Date prevEventMouseButtonTime;
    /**
     * флаг того, что окно развёрнуто на весь экран
     */
    private boolean maximizedWindow;
    /**
     * Панель информации
     */
    private final PanelInfo panelInfo;
    /**
     * Текущий режим(по умолчанию рабочий)
     */
    public static Mode currentMode = Mode.WORK;
    /**
     * Панель игры
     */
    private final PanelGame panelGame;

    /**
     * Конструктор окна приложения
     */
    public Application() {
        // создаём окно
        window = App.makeWindow();

        // панель информации
        panelInfo = new PanelInfo(window, true, DIALOG_BACKGROUND_COLOR, PANEL_PADDING);

        // панель игры
        panelGame = new PanelGame(window, true, DIALOG_BACKGROUND_COLOR, PANEL_PADDING);

        // задаём обработчиком событий текущий объект
        window.setEventListener(this);
        // задаём заголовок
        window.setTitle("Java 2D");
        // задаём размер окна
        window.setWindowSize(900, 900);
        // задаём его положение
        window.setWindowPosition(100, 100);

        // задаём иконку
        switch (Platform.CURRENT) {
            case WINDOWS -> window.setIcon(new File("src/main/resources/windows.ico"));
            case MACOS -> window.setIcon(new File("src/main/resources/macos.icns"));
        }

        // названия слоёв, которые будем перебирать
        String[] layerNames = new String[]{
                "LayerGLSkija", "LayerRasterSkija"
        };

        // перебираем слои
        for (String layerName : layerNames) {
            String className = "io.github.humbleui.jwm.skija." + layerName;
            try {
                Layer layer = (Layer) Class.forName(className).getDeclaredConstructor().newInstance();
                window.setLayer(layer);
                break;
            } catch (Exception e) {
                System.out.println("Ошибка создания слоя " + className);
            }
        }

        // если окну не присвоен ни один из слоёв
        if (window._layer == null)
            throw new RuntimeException("Нет доступных слоёв для создания");

        // делаем окно видимым
        window.setVisible(true);
    }

    /**
     * Обработчик событий
     *
     * @param e событие
     */
    @Override
    public void accept(Event e) {
        // если событие кнопка мыши
        if (e instanceof EventMouseButton) {
            // получаем текущие дату и время
            Date now = Calendar.getInstance().getTime();
            // если уже было нажатие
            if (prevEventMouseButtonTime != null) {
                // если между ними прошло больше 200 мс
                long delta = now.getTime() - prevEventMouseButtonTime.getTime();
                if (delta < 200)
                    return;
            }
            // сохраняем время последнего события
            prevEventMouseButtonTime = now;
        }
        // кнопки клавиатуры
        else if (e instanceof EventKey eventKey) {
            // кнопка нажата с Ctrl
            if (eventKey.isPressed()) {
                if (eventKey.isModifierDown(MODIFIER))
                    // разбираем, какую именно кнопку нажали
                    switch (eventKey.getKey()) {
                        case W -> window.close();
                        case H -> window.minimize();
                        case DIGIT1 -> {
                            if (maximizedWindow)
                                window.restore();
                            else
                                window.maximize();
                            maximizedWindow = !maximizedWindow;
                        }
                        case DIGIT2 -> window.setOpacity(window.getOpacity() == 1f ? 0.5f : 1f);
                    }
                else
                    switch (eventKey.getKey()) {
                        case ESCAPE -> {
                            if (currentMode.equals(Mode.WORK)) {
                                window.close();
                                // завершаем обработку, иначе уже разрушенный контекст
                                // будет передан панелям
                                return;
                            }
                        }
                        case TAB -> InputFactory.nextTab();
                    }
            }
        }
        // если событие - это закрытие окна
        else if (e instanceof EventWindowClose) {
            // завершаем работу приложения
            App.terminate();
            // закрытие окна
        } else if (e instanceof EventWindowCloseRequest) {
            window.close();
        } else if (e instanceof EventFrameSkija ee) {
            Surface s = ee.getSurface();
            paint(s.getCanvas(), new CoordinateSystem2i(0, 0, s.getWidth(), s.getHeight())
            );
        } else if (e instanceof EventFrame) {
            // запускаем рисование кадра
            window.requestFrame();
        }

        switch (currentMode) {
            case INFO -> panelInfo.accept(e);
            case WORK -> panelGame.accept(e);
        }
    }

    /**
     * Рисование
     *
     * @param canvas   низкоуровневый инструмент рисования примитивов от Skija
     * @param windowCS СК окна
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // запоминаем изменения (пока что там просто заливка цветом)
        canvas.save();
        // очищаем канвас
        canvas.clear(APP_BACKGROUND_COLOR);
        // рисуем панели
        panelGame.paint(canvas, windowCS);
        canvas.restore();

        // рисуем диалоги
        switch (currentMode) {
            case INFO -> panelInfo.paint(canvas, windowCS);
        }
    }
}
