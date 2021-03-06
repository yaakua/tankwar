package cn.doitoo.game.tankwar.event;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import cn.doitoo.game.framework.arithmetic.PathSolver;
import cn.doitoo.game.framework.context.G;
import cn.doitoo.game.framework.event.TouchEventHandler;
import cn.doitoo.game.framework.map.DoitooMap;
import cn.doitoo.game.framework.util.CoordinateUtil;
import cn.doitoo.game.framework.util.Util;
import cn.doitoo.game.tankwar.effect.ClickEffect;
import cn.doitoo.game.tankwar.effect.SelectEffect;
import cn.doitoo.game.tankwar.role.tank.player.PlayerHeroTank;

public class GestureMoveEvent extends TouchEventHandler {

    private int preX = -1;
    private int preY = -1;
    private DoitooMap map;
    private int maxWidth;
    private int maxHeight;
    private PlayerHeroTank player;
    private int[][] gameMap01Vector;
    private PathSolver pathSolver;

    public GestureMoveEvent() {
        map = G.getDoitooMap();
        player = (PlayerHeroTank) G.get("playerHeroTankTask");
        int screenHeight = G.getInt("screenHeight");
        int screenWidth = G.getInt("screenWidth");
        maxWidth = screenWidth - map.getWidth() - 48;
        maxHeight = screenHeight - map.getHeight() - 48;
        gameMap01Vector = (int[][]) G.get("gameMap01Vector");
        pathSolver = (PathSolver) G.get("pathSolver");
    }

    public void onTouchDown(MotionEvent event) {
        preX = (int) event.getX();
        preY = (int) event.getY();
    }

    public void onTouchMove(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        int deltaX = x - preX;
        int deltaY = y - preY;

        if (Math.abs(deltaX) < 5 && Math.abs(deltaY) < 5)
            return;

        int px = Math.min(Math.max(map.getX() + deltaX, maxWidth), 48);
        int py = Math.min(Math.max(map.getY() + deltaY, maxHeight), 48);
        map.setPosition(px, py);
        preX = x;
        preY = y;

    }

    public void onTouchUp(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        int rect = 2;
        if (Math.abs(x - preX) < rect && Math.abs(y - preY) < rect) {

            if (player != null) {
                int playerX = player.getX();
                int playerY = player.getY();
                Point startNodePoint = new Point(playerX, playerY);
                CoordinateUtil.world2screen(startNodePoint);
                //动态更新当前坦克可点击的范围
                Rect playerCurrentRect = new Rect(startNodePoint.x, startNodePoint.y, (startNodePoint.x + player.getWidth()), (startNodePoint.y + player.getHeight()));

                // 如果点击的是坦克判断当前坦克是否被选中，如果选中才进行移动
                if (playerCurrentRect.contains(preX, preY)) {
                    //添加选中特效动画
                    SelectEffect effect = (SelectEffect) player.getEffectByKey("circle0");
                    if (player.isSelected()) {
                        if (effect != null) {
                            player.deleteEffect("circle0");
                        }
                        player.setSelected(false);
                    } else {
//                        if (effect == null)
//                            effect = new SelectEffect(playerX, playerY);
//                        effect.setMoving(true);//设置为可移动（根据坦克坐标进行移动）
//                        player.addSelectedEffect("circle0", effect);
//                        player.setSelected(true);
                    }
                } else {
                    if (!player.isSelected())
                        return;
                }

                if (playerCurrentRect.contains(x, y)) {
                    return;
                }

                Point endNodePoint = new Point(preX, preY);
                CoordinateUtil.screen2world(endNodePoint);

                //点击特效动画
                ClickEffect clickEffect = (ClickEffect) player.getEffectByKey("clickEffect");
                if (clickEffect == null)
                    clickEffect = new ClickEffect(endNodePoint.x, endNodePoint.y);
                else
                    clickEffect.setPosition(endNodePoint.x, endNodePoint.y);
                player.addEffect("clickEffect", clickEffect);

                //坦克坐标需要转换成世界坐标
                CoordinateUtil.screen2world(startNodePoint);
                player.setPathList(Util.computeShortestPath(startNodePoint, endNodePoint));

            }
        }
    }


}

