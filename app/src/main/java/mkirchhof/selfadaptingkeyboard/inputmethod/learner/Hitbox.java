/*
 * Copyright (C) 2020 Michael Kirchhof
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mkirchhof.selfadaptingkeyboard.inputmethod.learner;

import java.io.Serializable;

public class Hitbox implements Serializable {
    private static final long serialVersionUID = 1L;

    private int mCode;
    private Point mTopLeft;
    private Point mTopRight;
    private Point mBottomLeft;
    private Point mBottomRight;

    public int getCode() {
        return mCode;
    }

    public Point getTopLeft() {
        return mTopLeft;
    }

    public void setTopLeft(Point topLeft) {
        this.mTopLeft = topLeft;
    }

    public Point getTopRight() {
        return mTopRight;
    }

    public void setTopRight(Point topRight) {
        this.mTopRight = topRight;
    }

    public Point getBottomLeft() {
        return mBottomLeft;
    }

    public void setBottomLeft(Point bottomLeft) {
        this.mBottomLeft = bottomLeft;
    }

    public Point getBottomRight() {
        return mBottomRight;
    }

    public void setBottomRight(Point bottomRight) {
        this.mBottomRight = bottomRight;
    }

    public int getWidth(){return mTopRight.getX() - mTopLeft.getX(); }

    public int getHeight(){ return mBottomLeft.getY() - mTopLeft.getY();}

    public void setTop(int top){
        mTopLeft.setY(top);
        mTopRight.setY(top);
    }

    public void setBottom(int bot){
        mBottomLeft.setY(bot);
        mBottomRight.setY(bot);
    }

    public void setLeft(int left){
        mTopLeft.setX(left);
        mBottomLeft.setX(left);
    }

    public void setRight(int right){
        mTopRight.setX(right);
        mBottomRight.setX(right);
    }

    public String toString(){
        return "Keycode: " + mCode + ", spanning from x = " + mTopLeft.getX() + " to " + mTopRight.getX() +
                " and y = " + mTopLeft.getY() + " to " + mBottomLeft.getY();
    }

    public Hitbox(int code, Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        this.mCode = code;
        this.mTopLeft = topLeft;
        this.mTopRight = topRight;
        this.mBottomLeft = bottomLeft;
        this.mBottomRight = bottomRight;
    }

    // returns whether a position is within the hitbox +/- 1*width and +/- 1*height
    public boolean isClose(int x, int y){
        return mTopLeft.getX() - getWidth() <= x &
                x <= mTopRight.getX() + getWidth() &
                mTopRight.getY() - getHeight() <= y &
                y <= mBottomLeft.getY() + getHeight();
    }
}
