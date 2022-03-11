package game;

import misc.Misc;
import misc.Vector2d;

/**
 * Класс игрока
 */
public class Player extends GameObject {
    /**
     * Конструктор игрового объекта
     *
     * @param pos   положение
     * @param speed скорость
     * @param acc   ускорение
     * @param size  размер
     */
    public Player(Vector2d pos, Vector2d speed, Vector2d acc, float size) {
        super(pos, speed, acc, size, Misc.getColor(150, 50, 200, 50));
    }

}
