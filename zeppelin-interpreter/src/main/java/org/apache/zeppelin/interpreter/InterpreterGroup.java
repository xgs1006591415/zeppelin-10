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

package org.apache.zeppelin.interpreter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.zeppelin.display.AngularObjectRegistry;
import org.apache.zeppelin.resource.ResourcePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InterpreterGroup is collections of interpreter sessions. One session could include multiple
 * interpreters. For example spark, pyspark, sql interpreters are in the same 'spark' interpreter
 * session.
 *
 * <p>Remember, list of interpreters are dedicated to a session. Session could be shared across user
 * or notes, so the sessionId could be user or noteId or their combination. So InterpreterGroup
 * internally manages map of [sessionId(noteId, user, or their combination), list of interpreters]
 *
 * <p>A InterpreterGroup runs interpreter process while its subclass ManagedInterpreterGroup runs in
 * zeppelin server process.
 */
public class InterpreterGroup {

  private static final Logger LOGGER = LoggerFactory.getLogger(InterpreterGroup.class);

  protected String id;
  // sessionId --> interpreters
  protected Map<String, List<Interpreter>> sessions = new ConcurrentHashMap();
  private AngularObjectRegistry angularObjectRegistry;
  private InterpreterHookRegistry hookRegistry;
  private ResourcePool resourcePool;
  private boolean angularRegistryPushed = false;

  /**
   * Create InterpreterGroup with given id, used in InterpreterProcess
   *
   * @param id
   */
  public InterpreterGroup(String id) {
    this.id = id;
  }

  /** Create InterpreterGroup with autogenerated id */
  public InterpreterGroup() {
    this.id = generateId();
  }

  private static String generateId() {
    return "InterpreterGroup_" + System.currentTimeMillis() + "_" + new SecureRandom().nextInt();
  }

  public String getId() {
    return this.id;
  }

  // TODO(zjffdu) change it to getSession. For now just keep this method to reduce code change
  public synchronized List<Interpreter> get(String sessionId) {
    return sessions.get(sessionId);
  }

  // TODO(zjffdu) change it to addSession. For now just keep this method to reduce code change
  public synchronized void put(String sessionId, List<Interpreter> interpreters) {
    this.sessions.put(sessionId, interpreters);
  }

  public synchronized void addInterpreterToSession(Interpreter interpreter, String sessionId) {
    LOGGER.debug("Add Interpreter {} to session {}", interpreter.getClassName(), sessionId);
    List<Interpreter> interpreters = get(sessionId);
    if (interpreters == null) {
      interpreters = new ArrayList<>();
    }
    interpreters.add(interpreter);
    put(sessionId, interpreters);
  }

  // TODO(zjffdu) rename it to a more proper name.
  // For now just keep this method to reduce code change
  public Collection<List<Interpreter>> values() {
    return sessions.values();
  }

  public AngularObjectRegistry getAngularObjectRegistry() {
    return angularObjectRegistry;
  }

  public void setAngularObjectRegistry(AngularObjectRegistry angularObjectRegistry) {
    this.angularObjectRegistry = angularObjectRegistry;
  }

  public InterpreterHookRegistry getInterpreterHookRegistry() {
    return hookRegistry;
  }

  public void setInterpreterHookRegistry(InterpreterHookRegistry hookRegistry) {
    this.hookRegistry = hookRegistry;
  }

  public int getSessionNum() {
    return sessions.size();
  }

  public void setResourcePool(ResourcePool resourcePool) {
    this.resourcePool = resourcePool;
  }

  public ResourcePool getResourcePool() {
    return resourcePool;
  }

  public boolean isAngularRegistryPushed() {
    return angularRegistryPushed;
  }

  public void setAngularRegistryPushed(boolean angularRegistryPushed) {
    this.angularRegistryPushed = angularRegistryPushed;
  }

  public boolean isEmpty() {
    return sessions.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterpreterGroup)) {
      return false;
    }

    InterpreterGroup that = (InterpreterGroup) o;

    return id != null ? id.equals(that.id) : that.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  public void close() {
    for (List<Interpreter> session : sessions.values()) {
      for (Interpreter interpreter : session) {
        try {
          interpreter.close();
        } catch (InterpreterException e) {
          LOGGER.warn("Fail to close interpreter: " + interpreter.getClassName(), e);
        }
      }
    }
    sessions.clear();
  }
}
