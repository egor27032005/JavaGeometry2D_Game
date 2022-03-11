package panels;


import controls.Label;
import controls.MultiLineLabel;
import game.Game;
import io.github.humbleui.jwm.Window;
import io.github.humbleui.skija.Canvas;
import misc.CoordinateSystem2i;

/**
 * Панель игры
 */
public class PanelGame extends Panel {
    /**
     * Игра
     */
    public static final Game game = Game.getGame();
    /**
     * Заголовок с информацией
     */
    private final MultiLineLabel infoLabel;
    /**
     * Заголовок подсказки
     */
    private final Label hintLabel;

    /**
     * Конструктор панели
     *
     * @param window          окно
     * @param drawBG          нужно ли рисовать подложку
     * @param backgroundColor цвет фона
     * @param padding         отступы
     */
    public PanelGame(Window window, boolean drawBG, int backgroundColor, int padding) {
        super(window, drawBG, backgroundColor, padding);
        // создаём заголовок
        infoLabel = new MultiLineLabel(window, false, backgroundColor, 0,
                7, 7, 6, 0, 1, 1,
                "Информация", true, true);
        // создаём заголовок
        hintLabel = new Label(window, false, backgroundColor, 0,
                7, 7, 0, 0, 3, 1,
                "Для прыжка нажмите ПРОБЕЛ", true, true);
    }


    /**
     * Метод под рисование в конкретной реализации
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    @Override
    public void paintImpl(Canvas canvas, CoordinateSystem2i windowCS) {
        // задаём текст заголовка
        infoLabel.text = String.format(
                "Пройдено %.1f\n Скорость: %.1f", game.getScore(), game.getNewEnemySpeed()
        );
        // рисуем заголовок информации
        infoLabel.paint(canvas, windowCS);
        // рисуем заголовок подсказки
        hintLabel.paint(canvas, windowCS);
        // обрабатываем игру
        game.process();
        // рисуем игру
        game.paint(canvas, windowCS);
    }

}
