/*
 * Copyright 2015 CptWin.
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
package nz.co.lolnet.statistics;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nz.co.lolnet.james137137.HttpThreadPool;

/**
 *
 * @author CptWin
 */
public class ThreadSendFeedback implements Runnable {
    
    private String message;
    private String meta;
    
    public ThreadSendFeedback(String message, String meta)
    {
        this.message = message;
        this.meta = meta;
        HttpThreadPool.add(this);
    }

    @Override
    public void run() {
        try {
            LauncherStatistics.sendFeedback(message, meta);
        } catch (IOException ex) {
            Logger.getLogger(ThreadLauncherIsLaunched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
