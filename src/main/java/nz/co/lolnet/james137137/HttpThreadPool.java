/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

/**
 *
 * @author James
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class HttpThreadPool {
    
    
    static ExecutorService executor = Executors.newFixedThreadPool(4);
    public static void add(Runnable object)
    {
        executor.execute(object);
    }
}
