package application.entity;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Poker extends FlipView
{
    private PokerColor color;
    public PokerColor getColor() { return color; }

    private int num;
    public int getNum() { return num; }

    private int pos;
    public int getPos() { return pos; }
    public void setPos(int pos) { this.pos = pos; }

    private static Image backImg =
            new Image(Poker.class.getResourceAsStream("../../res/drawable/back.png"));

    public Poker()
    {
        super();
    }

    public Poker(PokerColor color, int num, double width, double height)
    {
        super(width, height);
        this.color = color;
        this.num = num;
        ImageView back = new ImageView(backImg);
        back.setFitWidth(width);
        back.setFitHeight(height);
        ImageView front = new ImageView(new Image(Poker.class.getResourceAsStream
                ("../../res/drawable/"+ color.toString().toLowerCase()+ "_" + num + ".png")));
        front.setFitWidth(width);
        front.setFitHeight(height);
        frontNode = back;
        backNode = front;
        init();
    }

}
