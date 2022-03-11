package game;

import misc.Misc;
import misc.Vector2d;

/**
 * Класс противника
 */
public class Enemy extends GameObject {
    /**
     * Конструктор противника
     *
     * @param pos   положение
     * @param speed скорость
     * @param acc   ускорение
     * @param size  размер
     */
    public Enemy(Vector2d pos, Vector2d speed, Vector2d acc, float size) {
        super(pos, speed, acc, size, Misc.getColor(200, 150, 50, 50));
    }

}
