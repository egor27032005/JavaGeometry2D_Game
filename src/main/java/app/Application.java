package app;

import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Surface;
import misc.CoordinateSystem2i;
import panels.PanelControl;
import panels.PanelHelp;
import panels.PanelLog;
import panels.PanelRendering;

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
     * панель легенды
     */
    private final PanelHelp panelHelp;
    /**
     * панель курсора мыши
     */
    private final PanelControl panelControl;
    /**
     * панель рисования
     */
    private final PanelRendering panelRendering;
    /**
     * панель событий
     */
    private final PanelLog panelLog;
    /**
     * время последнего нажатия клавиши мыши
     */
    Date prevEventMouseButtonTime;

    /**
     * Конструктор окна приложения
     */
    public Application() {
        // создаём окно
        window = App.makeWindow();

        // создаём панель рисования
        panelRendering = new PanelRendering(
                window, true, PANEL_BACKGROUND_COLOR, PANEL_PADDING, 5, 3, 0, 0,
                3, 2
        );
        // создаём панель управления
        panelControl = new PanelControl(
                window, true, PANEL_BACKGROUND_COLOR, PANEL_PADDING, 5, 3, 3, 0,
                2, 2
        );
        // создаём панель лога
        panelLog = new PanelLog(
                window, true, PANEL_BACKGROUND_COLOR, PANEL_PADDING, 5, 3, 0, 2,
                3, 1
        );
        // создаём панель помощи
        panelHelp = new PanelHelp(
                window, true, PANEL_BACKGROUND_COLOR, PANEL_PADDING, 5, 3, 3, 2,
                2, 1
        );

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

        // если событие - это закрытие окна
        if (e instanceof EventWindowClose) {
            // завершаем работу приложения
            App.terminate();
        } else if (e instanceof EventWindowCloseRequest) {
            window.close();
        } else if (e instanceof EventFrameSkija ee) {
            Surface s = ee.getSurface();
            paint(s.getCanvas(), new CoordinateSystem2i(0, 0, s.getWidth(), s.getHeight())
            );
        }

        // передаём события на обработку панелям
        panelControl.accept(e);
        panelRendering.accept(e);
        panelLog.accept(e);
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
        panelRendering.paint(canvas, windowCS);
        panelControl.paint(canvas, windowCS);
        panelLog.paint(canvas, windowCS);
        panelHelp.paint(canvas, windowCS);
        canvas.restore();
    }
}
