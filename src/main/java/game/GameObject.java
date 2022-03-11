package game;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.RRect;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;
import misc.Vector2i;

/**
 * Игровой объект
 */
public class GameObject {
    /**
     * Положение
     */
    public Vector2d pos;
    /**
     * Скорость
     */
    public Vector2d speed;
    /**
     * Ускорение
     */
    public Vector2d acc;
    /**
     * Размер
     */
    private final float size;
    /**
     * Цвет
     */
    private final int color;

    /**
     * Конструктор игрового объекта
     *
     * @param pos   положение
     * @param speed скорость
     * @param acc   ускорение
     * @param size  размер
     * @param color цвет
     */
    public GameObject(Vector2d pos, Vector2d speed, Vector2d acc, float size, int color) {
        this.pos = pos;
        this.speed = speed;
        this.acc = acc;
        this.size = size;
        this.color = color;
    }

    /**
     * Метод обработки
     * <p>
     * При наследовании необходимо вызывать обработку предка super.process(dt)
     *
     * @param dt изменение времени
     */
    public void process(double dt) {
        pos.add(speed.mult(dt));
        speed.add(acc.mult(dt));
    }

    /**
     * Рисование
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     * @param ownCS    СК игры
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS, CoordinateSystem2d ownCS) {
        try (Paint paint = new Paint().setColor(color)) {
            Vector2i windowPos = windowCS.getCoords(pos, ownCS);
            canvas.drawRRect(RRect.makeXYWH(
                    windowPos.x - size / 2,
                    windowCS.getMax().y - windowPos.y - size / 2,
                    size, size, 4), paint);
        }
    }
}
