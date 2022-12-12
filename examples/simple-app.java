// Copyright 2013-2019 Azul Systems, Inc.  All Rights Reserved.
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//
// This code is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License version 2 only, as published by
// the Free Software Foundation.
//
// This code is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE.  See the GNU General Public License version 2 for more
// details (a copy is included in the LICENSE file that accompanied this code).
//
// You should have received a copy of the GNU General Public License version 2
// along with this work; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
//
// Please contact Azul Systems, 385 Moffett Park Drive, Suite 115, Sunnyvale,
// CA 94089 USA or visit www.azul.com if you need additional information or
// have any questions.



import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

//import jdk.crac.*;
//import jdk.crac.management.*;
import org.crac.management.*;
import org.crac.*;

class GetThreadID {
    public static native int get_tid();

    static {
        System.loadLibrary("GetThreadID");
    }
}

class App {
    public static void main(String args[]) throws Exception {
        Test test = new Test();
        test.Start();
    }
}

class Test {
    public static void Start() throws Exception {

        System.out.println("init");

        TestResource r = new TestResource();
        Core.getGlobalContext().register(r);

        System.out.println("Current Process ID: " + ProcessHandle.current().pid());

        System.out.println("start");

        if (System.getProperty("preLoop") != null) {
            System.out.println("property preLoop");
            int cnt = 0;
            while (true) {
                System.out.println("stage 1: " + ++cnt);
                Thread.sleep(1000);
            }
        }

        if (System.getProperty("callCR") != null) {
            System.out.println("property callCR");
            Core.checkpointRestore();
        }

        System.out.println("finish");

        CRaCMXBean cracMXBean = CRaCMXBean.getCRaCMXBean();
        System.out.println("UptimeSinceRestore: " + cracMXBean.getUptimeSinceRestore());

        System.out.println(
                DateTimeFormatter.ofPattern("E dd LLL yyyy HH:mm:ss.n").format(
                        Instant.ofEpochMilli(cracMXBean.getRestoreTime())
                                .atZone(ZoneId.systemDefault())));

        if (System.getProperty("postLoop") != null) {
            int cnt = 0;
            while (true) {
                System.out.println("stage 2: " + ++cnt);
                Thread.sleep(1000);
            }
        }
    }
}

class TestResource implements jdk.crac.Resource {

    @Override
    public void beforeCheckpoint(Context<? extends jdk.crac.Resource> context) throws Exception {
        System.out.println("beforeCheckpoint");
        if (System.getProperty("checkpointException") != null) {
            throw new RuntimeException("checkpoint");
        }
    }

    @Override
    public void afterRestore(Context<? extends jdk.crac.Resource> context) throws Exception {
        System.out.println("afterRestore");
        if (System.getProperty("restoreException") != null) {
            throw new RuntimeException("restore");
        }
    }
}

