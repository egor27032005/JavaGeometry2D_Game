package game;

import dialogs.PanelGameInfo;
import io.github.humbleui.skija.Canvas;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;

import java.util.*;

/**
 * Синглтон класс игры
 */
public class Game {
    /**
     * Размер игрока
     */
    private static final float PLAYER_SIZE = 40;
    /**
     * Начальная скорость игрока при прыжке
     */
    private static final float PLAYER_JUMP_SPEED = 3000;
    /**
     * Размер противника
     */
    private static final float ENEMY_SIZE = 40;
    /**
     * Ускорение врагов
     */
    private static final float ENEMY_ACC = 5.0f;
    /**
     * Ускорение врагов
     */
    private static final float ENEMY_START_SPEED = 300.0f;
    /**
     * СК игры
     */
    private final CoordinateSystem2d ownCS;
    /**
     * Результат игры
     */
    private double score;
    /**
     * Последняя полученная СК окна
     */
    private CoordinateSystem2i lastWindowCS;
    /**
     * Предыдущее время обработки
     */
    private static Date prevTime;
    /**
     * Флаг, остановлена ли игра
     */
    private boolean paused;
    /**
     * сама игра
     */
    private static Game thisGame;
    /**
     * Игрок
     */
    private Player player;
    /**
     * Враги
     * хранятся в LinkedList, потому что часто удаляются и добавляются
     */
    private final List<Enemy> enemies = new LinkedList<>();
    /**
     * Скорость у новых врагов при появлении
     */
    private float newEnemySpeed;

    /**
     * Конструктор игры
     */
    private Game() {
        // инициализируем СК игры
        ownCS = new CoordinateSystem2d(-50, -300, 1000, 1000);
        // перезапускаем игру
        restart();
    }

    /**
     * Получить ссылку на игру
     *
     * @return синглтон игры
     */
    public static Game getGame() {
        if (thisGame == null)
            thisGame = new Game();
        return thisGame;
    }

    /**
     * Перезапустить игру
     */
    public void restart() {
        // результат равен 0
        score = 0;
        // игра запущена
        paused = false;
        // создаём игрока, на которого действует гравитация по оси X
        player = new Player(
                new Vector2d(0, 0), new Vector2d(0, 0), new Vector2d(0, -9000), PLAYER_SIZE
        );
        // скорость врага берётся со знаком минус, т.к. враги
        // формируются на правом краю экрана и движутся влево,
        // т.е. против оси X
        newEnemySpeed = -ENEMY_START_SPEED;
        // очищаем список врагов
        enemies.clear();
    }


    /**
     * Получить скорость нового врага
     *
     * @return скорость нового врага
     */
    public float getNewEnemySpeed() {
        return Math.abs(newEnemySpeed);
    }


    /**
     * Получить время с прошлого вызова
     *
     * @return сколько времени прошло с момента прошлого вызова этого метода
     */
    private static double getDeltaTime() {
        // если время ещё ни разу не сохранялось
        if (prevTime == null) {
            // сохраняем его
            prevTime = Calendar.getInstance().getTime();
            // возвращаем 0
            return 0;
        }
        // получаем текущие дату и время
        Date now = Calendar.getInstance().getTime();
        // получаем, сколько времени прошло с прошлого запуска в миллисекундах
        long delta = now.getTime() - prevTime.getTime();
        // сохраняем время запуска
        prevTime = now;
        // возвращаем время в секундах
        return (double) delta / 1e3;
    }


    /**
     * Обработка игры
     */
    public void process() {
        // если игра на паузе
        if (paused)
            // останавливаем её обработку
            return;

        // рассчитываем, сколько времени прошло с прошлой обработки
        double dt = getDeltaTime();
        // обработка игрока
        processPlayer(dt);
        processEnemies(dt);
    }

    /**
     * Обработка игрока
     *
     * @param dT сколько времени прошло, с прошлой обработки в секундах
     */
    private void processPlayer(double dT) {
        // обрабатываем игрока
        player.process(dT);
        // если игрок должен "уйти под землю"
        if (player.pos.y <= 0) {
            // обнуляем его положение
            player.pos.y = 0;
            // обнуляем скорость
            player.speed = new Vector2d(0, 0);
        }

        // перебираем врагов
        for (Enemy enemy : enemies)
            // если есть контакт с врагом
            if (Vector2d.distance(player.pos, enemy.pos) < (PLAYER_SIZE + ENEMY_SIZE) / 2)
                // оканчиваем игру
                gameOver();

        // вычитаем из результата пройденное расстояние самым быстрым врагом
        // просто само расстояние как функция от скорости отрицательное
        // поэтому для увеличения положительного результата мы
        // вычетаем его
        score -= newEnemySpeed * dT;
    }

    /**
     * Прыжок
     */
    public void up() {
        if (player.pos.y == 0)
            player.speed.y = PLAYER_JUMP_SPEED;
    }

    /**
     * Получить результат
     *
     * @return результат
     */
    public double getScore() {
        return score;
    }

    /**
     * Рисование задачи
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // сохраняем область рисования
        canvas.save();
        // рисуем игрока
        player.paint(canvas, windowCS, ownCS);
        // рисуем врагов
        for (Enemy enemy : enemies) {
            enemy.paint(canvas, windowCS, ownCS);
        }
        // восстанавливаем область рисования
        canvas.restore();
        // сохраняем СК экрана
        lastWindowCS = windowCS;
    }


    /**
     * Завершение игры
     */
    private void gameOver() {
        // останавливаем игру
        paused = true;
        // выводим панель информации
        PanelGameInfo.show("Игра окончена\nВаш результат: " + String.format("%.1f", score));
    }


    /**
     * Обрабатываем врагов
     *
     * @param dT время, прошедшее с предыдущей обработки в секундах
     */
    private void processEnemies(double dT) {
        // составляем список врагов, которых уже можно удалить, потому что
        // удалять элементы из того же списка, который читаем нельзя
        List<Enemy> enemiesToRemove = new ArrayList<>();
        //  перебираем врагов
        for (Enemy enemy : enemies) {
            // если враг выходит за СК игры
            if (!ownCS.checkCoords(enemy.pos)) {
                // добавляем его в список на удаление
                enemiesToRemove.add(enemy);
            }
        }
        // удаляем всех игроков
        enemies.removeAll(enemiesToRemove);

        // запускаем обработку всех оставшихся врагов
        for (Enemy enemy : enemies)
            enemy.process(dT);

        // довольно редко срабатывающий рандом
        if (Math.random() < 0.01)
            // добавляем нового врага
            addEnemy();

        // увеличиваем скорость нового врага на
        newEnemySpeed -= ENEMY_ACC * dT;
    }


    /**
     * Добавить врага
     */
    private void addEnemy() {
        if (lastWindowCS == null)
            return;
        if (!enemies.isEmpty() && enemies.get(enemies.size() - 1).pos.x > ownCS.getSize().x / 2) {
            return;
        }
        enemies.add(new Enemy(
                new Vector2d(ownCS.getMax().x - 20, 0),
                new Vector2d(newEnemySpeed, 0),
                new Vector2d(ENEMY_ACC, 0),
                ENEMY_SIZE
        ));
    }


}
