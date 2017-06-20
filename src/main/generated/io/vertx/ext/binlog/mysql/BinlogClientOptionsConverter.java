/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.binlog.mysql;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link io.vertx.ext.binlog.mysql.BinlogClientOptions}.
 *
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.binlog.mysql.BinlogClientOptions} original class using Vert.x codegen.
 */
public class BinlogClientOptionsConverter {

  public static void fromJson(JsonObject json, BinlogClientOptions obj) {
    if (json.getValue("connectTimeout") instanceof Number) {
      obj.setConnectTimeout(((Number)json.getValue("connectTimeout")).longValue());
    }
    if (json.getValue("filename") instanceof String) {
      obj.setFilename((String)json.getValue("filename"));
    }
    if (json.getValue("heartbeatInterval") instanceof Number) {
      obj.setHeartbeatInterval(((Number)json.getValue("heartbeatInterval")).longValue());
    }
    if (json.getValue("host") instanceof String) {
      obj.setHost((String)json.getValue("host"));
    }
    if (json.getValue("keepAlive") instanceof Boolean) {
      obj.setKeepAlive((Boolean)json.getValue("keepAlive"));
    }
    if (json.getValue("keepAliveInterval") instanceof Number) {
      obj.setKeepAliveInterval(((Number)json.getValue("keepAliveInterval")).longValue());
    }
    if (json.getValue("password") instanceof String) {
      obj.setPassword((String)json.getValue("password"));
    }
    if (json.getValue("port") instanceof Number) {
      obj.setPort(((Number)json.getValue("port")).intValue());
    }
    if (json.getValue("position") instanceof Number) {
      obj.setPosition(((Number)json.getValue("position")).longValue());
    }
    if (json.getValue("username") instanceof String) {
      obj.setUsername((String)json.getValue("username"));
    }
  }

  public static void toJson(BinlogClientOptions obj, JsonObject json) {
    json.put("connectTimeout", obj.getConnectTimeout());
    if (obj.getFilename() != null) {
      json.put("filename", obj.getFilename());
    }
    json.put("heartbeatInterval", obj.getHeartbeatInterval());
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    json.put("keepAlive", obj.isKeepAlive());
    json.put("keepAliveInterval", obj.getKeepAliveInterval());
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    json.put("port", obj.getPort());
    json.put("position", obj.getPosition());
    if (obj.getUsername() != null) {
      json.put("username", obj.getUsername());
    }
  }
}