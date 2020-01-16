package application.entity;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.PerspectiveTransform;
import javafx.util.Duration;

import java.util.Timer;

public abstract class FlipView extends Parent
{
    //正面视图
    public Node frontNode;
    //反面视图
    public Node backNode;
    //是否翻转
    boolean flipped = false;
    //翻转角度
    DoubleProperty time = new SimpleDoubleProperty(Math.PI / 2);
    //翻转时间
    public final static double duration = 0.28;
    //正面翻转特效
    PerspectiveTransform frontEffect = new PerspectiveTransform();
    //反面翻转特效
    PerspectiveTransform backEffect = new PerspectiveTransform();
    //翻转动画
    Timeline flipAnim = new Timeline();
    //移动时间
    DoubleProperty moveTime;
    //移动动画
    Timeline moveAnim = new Timeline();

    private long delay;

    double width, height, radius, back;

    public FlipView()
    {}

    public FlipView(double width, double height)
    {
        this.width = width;
        this.height = height;
        radius = width / 2;
        back = height / 10;
    }

    public FlipView(Node frontNode, Node backNode, double width, double height)
    {
        this.frontNode = frontNode;
        this.backNode = backNode;
        this.width = width;
        this.height = height;
        radius = width / 2;
        back = height / 10;
        init();
    }

    public void flip()
    {
        flipAnim.play();
        setDelay(0);
    }

    public void flipAndMove(double x, double y)
    {
        flipAnim.play();
        move(x, y);
        setDelay(0);
    }

    public boolean isFlipped()
    {
        return flipped;
    }

    public void restore()
    {
        if(flipped)
        {
            time.setValue(Math.PI/2);
            setPT(frontEffect, Math.PI/2);
            frontNode.setEffect(frontEffect);
            setPT(backEffect, Math.PI/2);
            backNode.setEffect(backEffect);
            flipped = !flipped;
            Node temp = frontNode;
            frontNode = backNode;
            backNode = frontNode;
        }
    }

    protected void init()
    {
        PerspectiveTransform pt = new PerspectiveTransform();
        setPT(pt, -Math.PI/2);
        backEffect.setInput(pt);

        time.addListener((ChangeListener) (arg0, arg1, arg2) ->
        {
            setPT(frontEffect, time.get());
            setPT(backEffect, time.get());
        });

        KeyFrame frame1 = new KeyFrame(Duration.ZERO, new KeyValue(time,
                Math.PI / 2, Interpolator.LINEAR));
        KeyFrame frame2 = new KeyFrame(Duration.seconds(duration),
                (EventHandler<ActionEvent>) event ->
                {
                    flipped = !flipped;
                    Node temp = frontNode;
                    frontNode = backNode;
                    backNode = frontNode;
                },
                new KeyValue(time, -Math.PI / 2, Interpolator.LINEAR));
        flipAnim.getKeyFrames().addAll(frame1, frame2);
        backNode.visibleProperty().bind(
                Bindings.when(time.lessThan(0)).then(true).otherwise(false));

        frontNode.visibleProperty().bind(
                Bindings.when(time.lessThan(0)).then(false).otherwise(true));
        setPT(frontEffect, time.get());
        setPT(backEffect, time.get());
        frontNode.setEffect(frontEffect);
        backNode.setEffect(backEffect);
        getChildren().addAll(backNode, frontNode);
    }

    private void setPT(PerspectiveTransform pt, double t)
    {
        pt.setUlx(radius - Math.sin(t) * radius);
        pt.setUly(0 + Math.cos(t) * back);
        pt.setUrx(radius + Math.sin(t) * radius);
        pt.setUry(0 - Math.cos(t) * back);
        pt.setLrx(radius + Math.sin(t) * radius);
        pt.setLry(height + Math.cos(t) * back);
        pt.setLlx(radius - Math.sin(t) * radius);
        pt.setLly(height - Math.cos(t) * back);
    }

    public void setDelay(long delay)
    {
        this.delay = delay;
        flipAnim.setDelay(Duration.millis(delay));
    }

    public void move(double x, double y)
    {
        moveAnim = new Timeline();
        moveAnim.setDelay(Duration.millis(delay));
        final KeyValue kvx = new KeyValue(this.translateXProperty(), x);
        final KeyValue kvy = new KeyValue(this.translateYProperty(), y);
        final KeyFrame kf = new KeyFrame(Duration.seconds(duration), kvx, kvy);
        moveAnim.getKeyFrames().add(kf);
        moveAnim.play();
    }

    public void addOnFinishedHandler(EventHandler<ActionEvent> e)
    {
        moveAnim.setOnFinished(e);
    }

}
