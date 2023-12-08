/**
 * @Description
 * @Created by fun4ai
 * @Date 2023/12/6 2:34
 */
public class Outer {
    private static int a = 1;
    private int b = 2;

    public static void main(String[] args) {
        Outer.Inner inner = new Outer.Inner();
    }



    static class Inner{
        public void doSth(){
            System.out.println(a);
        }
    }
}