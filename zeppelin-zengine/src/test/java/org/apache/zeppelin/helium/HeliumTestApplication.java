/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.helium;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.zeppelin.resource.ResourceSet;

public class HeliumTestApplication extends Application {
  private AtomicInteger numRun = new AtomicInteger(0);

  public HeliumTestApplication(ApplicationContext context) {
    super(context);
  }

  @Override
  public void run(ResourceSet args) throws ApplicationException {
    try {
      context().out.clear();
      context().out.write("Hello world " + numRun.incrementAndGet());
      context().out.flush();
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  @Override
  public void unload() throws ApplicationException {}
}
