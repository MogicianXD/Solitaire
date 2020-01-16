package application.controller;

import application.entity.Poker;
import application.entity.PokerColor;
import application.util.AudioManager;
import application.util.AudioType;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.*;

public class Controller {

    @FXML
    private GridPane gridpane;

    @FXML
    private JFXButton btn_restart;

    @FXML
    private AnchorPane deck_pile;

    @FXML
    private AnchorPane discard_pile;

    @FXML
    private AnchorPane suit_pile_1;

    @FXML
    private AnchorPane suit_pile_2;

    @FXML
    private AnchorPane suit_pile_3;

    @FXML
    private AnchorPane suit_pile_4;

    @FXML
    private Text clock;

    @FXML
    private Text step_counter;

    @FXML
    private JFXComboBox combo_box;

    private int step;
    private double width, height;

    private final int POKER_COLOR_SUM = 13;
    private final int POKER_SUM = 4*13;
    private final int VGAP = 32;
    private final int HGAP = 24;
    private final int MAX_FLIP_SHOW_COUNT = 3;
    private int flipNum = 1;

    private AnchorPane[] tablePile;
    private AnchorPane[] suitPile;

    private PokerStack[] tableStack;
    private PokerStack discardStack;
    private PokerStack deckStack;
    private PokerStack[] suitStack;

    private TimerTask timerTask;
    private SimpleStringProperty time;

    private AudioManager audioManager = new AudioManager();

    //动画阻塞
    private boolean initing = false;
    private boolean moving = false;

    private boolean inited = false;

    public void init()
    {
        if(!inited)
        {
            Label[] label = new Label[3];
            for(int i = 0; i < 3; i++)
            {
                label[i] = new Label("翻 " + (i+1) + " 张");
                label[i].setStyle("-fx-font-size: 18");
                combo_box.getItems().add(label[i]);
            }
            combo_box.setValue(label[0]);
            combo_box.getSelectionModel().selectedItemProperty()
                    .addListener((observable, oldValue, newValue) ->
            {
                flipNum = Integer.parseInt(
                        ((Label)newValue).getText().substring(2,3));
            });
            inited = true;
        }


        initing = true;
        moving = false;
        step = 0;
        step_counter.setText("总步数：" + step);
        width = deck_pile.getMaxWidth();
        height = deck_pile.getMaxHeight();

        suitPile = new AnchorPane[4];
        suitStack = new PokerStack[4];
        suitPile[0] = suit_pile_1;
        suitPile[1] = suit_pile_2;
        suitPile[2] = suit_pile_3;
        suitPile[3] = suit_pile_4;
        for(int i = 0; i < 4; i++)
            suitStack[i] = new PokerStack(suitPile[i]);

        discardStack = new PokerStack(discard_pile);
        deckStack = new PokerStack(deck_pile);
        for(int i = 1; i <= POKER_COLOR_SUM; i++)
        {
            deckStack.push(new Poker(PokerColor.Club, i, width, height));
            deckStack.push(new Poker(PokerColor.Diamond, i, width, height));
            deckStack.push(new Poker(PokerColor.Heart, i, width, height));
            deckStack.push(new Poker(PokerColor.Spade, i, width, height));
        }
        deckStack.shuffle();
        deck_pile.setOnMouseClicked(event ->
        {
            if(moving)
                return;

            step_counter.setText("总步数：" + ++step + "");

            if(deckStack.empty())
            {
                shuffle();
                return;
            }
            moving = true;
            int size = discardStack.size();
            int leftShow = size > MAX_FLIP_SHOW_COUNT ? MAX_FLIP_SHOW_COUNT : size;
            if(size > 0)
            {
                removeDragListener(discardStack.peek());
                int surplus = flipNum - (MAX_FLIP_SHOW_COUNT - leftShow);
                if(surplus > 0)
                {
                    for (int i = leftShow - 1; i >= 1; i--)
                    {
                        int distance = leftShow - i < surplus ? leftShow - i : surplus;
                        Poker poker = discardStack.get(size - i);
                        TranslateTransition tt = moveBy(poker, -distance * HGAP, 0
                                , 125, 75 * (leftShow - i - 1));
                    }
                    leftShow = MAX_FLIP_SHOW_COUNT - flipNum;
                }
            }

            for(int i = 0; i < flipNum; i++)
            {
                if(deckStack.empty())
                    break;

                audioManager.playAudio(AudioType.Flip, 250*i);

                Poker poker = deckStack.peek();
                poker.setTranslateX(width + gridpane.getHgap());
                poker.setDelay(100 * i);
                poker.flipAndMove(-(MAX_FLIP_SHOW_COUNT - leftShow - i - 1) * HGAP, 0);
                deckStack.popTo(discardStack);
                if(i == flipNum - 1)
                    poker.addOnFinishedHandler(event1 ->
                    {
                        moving = false;
                        addDragListioner(poker, false);
                    });
            }
        });

        time = new SimpleStringProperty("00:00");
        Timer timer = new Timer();
        if(timerTask != null)
            timerTask.cancel();
        timerTask = new TimerTask() {
            long t = 0;
            DecimalFormat df = new DecimalFormat("00");
            public void run() {
                t++;
                long mm = t / 60 % 60;
                long ss = t % 60;
                time.set(df.format(mm) + ':' + df.format(ss));
            }
        };
        timer.schedule(timerTask, 0, 1000);
        clock.textProperty().bind(time);

        tablePile = new AnchorPane[7];
        tableStack = new PokerStack[7];
        int count = 0;
        for(int i = 0; i < 7; i++)
        {
            tablePile[i] = new AnchorPane();
            gridpane.add(tablePile[i], i, 2);
            tableStack[i] = new PokerStack(tablePile[i]);
            tablePile[i].toBack();
            for(int j = 0; j <= i; j++)
            {
//                Poker poker = deckStack.popTo(tableStack[i]);
                Poker poker = deckStack.popOnlyElement();
                //动画
                TranslateTransition tt = moveBy(poker, -(width + gridpane.getHgap()) *(6 - i),
                        height + gridpane.getVgap() + VGAP * j, 100, 100*count++);
                poker.setPos(i);
                int finalI = i;
                int finalJ = j;
                tt.setOnFinished(event ->
                {
                    deckStack.removeChildren(poker);
                    tableStack[finalI].push(poker);
                    poker.setTranslateX(0);
                    poker.setTranslateY(VGAP * finalJ);
                    if(finalJ == finalI)
                    {
                        poker.flip();
                        addDragListioner(poker, true);
                        if(finalI == 6)
                            initing = false;
                    }
                });
            }
        }
    }

    void shuffle()
    {
        for (Poker p : discardStack)
        {
            removeDragListener(p);
            p.setTranslateX(0);
            p.restore();
        }
        audioManager.playAudio(AudioType.Shuffle);
        discardStack.shuffle();
        discard_pile.getChildren().removeAll(discardStack);
        deckStack.addAll(discardStack);
        discardStack = new PokerStack(discard_pile);
        deck_pile.getChildren().addAll(deckStack);
    }

    double oldX = 0;
    double oldY = 0;
    double oldTranslateX = 0;
    int order = 0;
    int pos = 0;

    private void addDragListioner(Poker poker, boolean isInTable)
    {
        poker.setOnMousePressed(event ->
        {
            pos = isInTable ? poker.getPos() : 5;
            poker.getParent().toFront();
            oldX = event.getSceneX();
            oldY = event.getSceneY();
            oldTranslateX = poker.getTranslateX();
        });

        poker.setOnMouseDragged(event ->
        {
            if(!isInTable)
            {
                poker.setTranslateX(event.getSceneX() - oldX + oldTranslateX);
                poker.setTranslateY(event.getSceneY() - oldY);
            }
            else
            {
                order = tableStack[pos].indexOf(poker);
                for(int i = order; i < tableStack[pos].size(); i++)
                {
                    tableStack[pos].get(i).setTranslateX(event.getSceneX() - oldX);
                    tableStack[pos].get(i).setTranslateY(event.getSceneY() - oldY + i * VGAP);
                }
            }
        });

        poker.setOnMouseReleased(event ->
        {
            double x = poker.getTranslateX();
            double y = poker.getTranslateY();
            int newPos = (int)((x + (x>0?width/2:-width/2)) / (width + gridpane.getHgap()))+pos;
            int size = 0;

            if(isInTable)
            {
                size = tableStack[pos].size();
                for(int i = order; i < size; i++)
                {
                    tableStack[pos].get(i).setTranslateX(0);
                    tableStack[pos].get(i).setTranslateY(i * VGAP);
                }
            }
            else
            {
                size = discardStack.size();
                poker.setTranslateX(oldTranslateX);
                poker.setTranslateY(0);
            }

            if(newPos < 0 || newPos > 6)
                return;

            if((!isInTable || isInTable && order == tableStack[pos].size() - 1
                    && -y > gridpane.getVgap() + height/2)
                && newPos <= 3
                && (suitStack[newPos].empty() && poker.getNum() == 1
                    || (!suitStack[newPos].empty()
                        && suitStack[newPos].peek().getColor() == poker.getColor()
                        && suitStack[newPos].peek().getNum() == poker.getNum() - 1)))
            {
                //移至suitPile
                suitStack[newPos].push(poker);
                poker.setTranslateX(0);
                poker.setTranslateY(0);
                removeDragListener(poker);
                completeTest();
                if(isInTable)
                    tableStack[pos].pop();
                else
                    discardStack.pop();
            }
            else if(canPutInTable(poker, newPos))
            {
                if (!isInTable)
                {
                    //从discard中拿出
                    poker.setPos(newPos);
                    poker.setTranslateX(0);
                    poker.setTranslateY(tableStack[newPos].size() * VGAP);
                    discardStack.popTo(tableStack[newPos]);
                    removeDragListener(poker);
                    addDragListioner(poker, true);
                }
                else if (newPos != pos)
                {
                    int oldSize = tableStack[newPos].size();
                    for (int i = order; i < size; i++)
                    {
                        Poker p = tableStack[pos].get(order);
                        p.setPos(newPos);
                        p.setTranslateX(0);
                        p.setTranslateY((oldSize + i - order) * VGAP);
                        tableStack[pos].removeTo(order, tableStack[newPos]);
                    }
                }
            }
            else
                return;

            if(!isInTable)
            {
                if(size > MAX_FLIP_SHOW_COUNT)
                {
                    for(int i = 0; i < MAX_FLIP_SHOW_COUNT-1; i++)
                        moveBy(discardStack.get(size-2-i), HGAP, 0, 125, 50*i);
                }
                if(!discardStack.empty())
                {
                    addDragListioner(discardStack.peek(), false);
                }
            }
            else
            {
                if (!tableStack[pos].empty())
                {
                    Poker newTop = tableStack[pos].peek();
                    if(!newTop.isFlipped())
                    {
                        newTop.flip();
                        addDragListioner(newTop, true);
                    }
                }
            }

            step_counter.setText("总步数：" + ++step + "");
        });
    }

    private boolean canPutInTable(Poker poker, int newPos)
    {
        if(tableStack[newPos].empty())
        {
            if(poker.getNum() == 13)
                return true;
            else
                return false;
        }
        Poker top = tableStack[newPos].peek();
        if(poker.getNum() == top.getNum() - 1)
        {
            PokerColor color1 = poker.getColor();
            PokerColor color2 = top.getColor();
            boolean tag1 = false, tag2 = false;
            if(color1 == PokerColor.Heart || color1 == PokerColor.Diamond)
                tag1 = true;
            if(color2 == PokerColor.Heart || color2 == PokerColor.Diamond)
                tag2 = true;
            if(tag1 ^ tag2)
                return true;
        }
        return false;
    }

    private void completeTest()
    {
        for(int i = 0; i < 4; i++)
            if(suitStack[i].empty() || suitStack[i].peek().getNum() != 13)
                return;

        audioManager.playAudio(AudioType.Victory);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("恭喜通关！");
        alert.setHeaderText(null);
        alert.setContentText("总步数：" + step
                + "用时: " + clock.getText().substring(0, 2) + "分"
                + clock.getText().substring(3) + "秒");
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();

        restart(new ActionEvent());
    }

    private void removeDragListener(Poker poker)
    {
        poker.setOnMousePressed(null);
        poker.setOnMouseDragged(null);
        poker.setOnMouseReleased(null);
    }

    private TranslateTransition moveBy(Poker poker, double x, double y, long duration, long delay)
    {
        audioManager.playAudio(AudioType.SendHeavy, delay);
        TranslateTransition tt = new TranslateTransition(Duration.millis(duration), poker);
        tt.setByX(x);
        tt.setByY(y);
        tt.setDelay(Duration.millis(delay));
        tt.play();
        return tt;
    }

    @FXML
    void restart(ActionEvent event)
    {
        if(initing)
            return;
        gridpane.getChildren().removeAll(tablePile);
        discard_pile.getChildren().removeAll(discardStack);
        deck_pile.getChildren().removeAll(deckStack);
        for(int i = 0; i < 4; i++)
            suitPile[i].getChildren().removeAll(suitPile[i].getChildren());
        init();
    }
}

class PokerStack extends Stack<Poker>
{
    Pane container;

    public PokerStack(Pane container)
    {
        super();
        this.container = container;
    }

    public Poker push(Poker poker)
    {
        super.push(poker);
        container.getChildren().add(poker);
        return poker;
    }

    public Poker pop()
    {
        Poker top = super.pop();
        container.getChildren().remove(top);
        return top;
    }

    public Poker popTo(PokerStack to)
    {
        to.push(peek());
        return super.pop();
    }

    public Poker popOnlyElement()
    {
        return super.pop();
    }

    public void removeChildren(Poker poker)
    {
        container.getChildren().remove(poker);
    }

    public void remove(Poker poker)
    {
        super.remove(poker);
        container.getChildren().remove(poker);
    }

    public Poker remove(int index)
    {
        Poker poker = super.remove(index);
        container.getChildren().remove(index);
        return poker;
    }

    public Poker removeTo(int index, PokerStack to)
    {
        Poker poker = get(index);
        to.push(poker);
        super.remove(index);
        return poker;
    }

    public void shuffle()
    {
        container.getChildren().removeAll(this);
        Collections.shuffle(this);
        container.getChildren().addAll(this);
    }

}
