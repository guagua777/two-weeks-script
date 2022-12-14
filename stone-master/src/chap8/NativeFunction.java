package chap8;
import java.lang.reflect.Method;
import stone.StoneException;
import stone.ast.ASTree;

public class NativeFunction {
    //java原生方法
    protected Method method;
    protected String name;
    protected int numParams;


//    protected Method method;
//    protected String name;
//    protected int numParams;


    public NativeFunction(String n, Method m) {
        name = n;
        method = m;
        numParams = m.getParameterTypes().length;
    }
    @Override public String toString() { return "<native:" + hashCode() + ">"; }
    public int numOfParameters() { return numParams; }

    //invoke方法
    public Object invoke(Object[] args, ASTree tree) {
        try {
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new StoneException("bad native function call: " + name, tree);
        }
    }

//    public Object invoke(Object[] args, ASTree tree) {
//        try {
//            //因为是静态方法，所以obj为null
//            return method.invoke(null, args);
//        } catch (Exception e) {
//            throw new StoneException("", tree);
//        }
//    }




}
