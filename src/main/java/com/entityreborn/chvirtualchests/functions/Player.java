/*
 * The MIT License
 *
 * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.entityreborn.chvirtualchests.functions;

import com.entityreborn.chvirtualchests.VirtualChests;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class Player {

    public static void closeVirtualChests() {
        for (String id : VirtualChests.getAll()) {
            for (MCHumanEntity p : VirtualChests.get(id).getViewers()) {
                if (p != null) {
                    try {
                        p.closeInventory();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @api(environments = {CommandHelperEnvironment.class})
    public static class popen_virtualchest extends AbstractFunction {

        public Class<? extends CREThrowable>[] thrown() {
            return new Class[]{CREFormatException.class};
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            MCPlayer p;
            String id;

            if (args.length == 2) {
                p = Static.GetPlayer(args[0], t);
                id = args[1].val();

                if (id.isEmpty() || args[1] instanceof CNull) {
                    throw new CREFormatException("invalid id. Use either a string or integer.", t);
                }
            } else {
                p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
                id = args[0].val();

                if (id.isEmpty() || args[0] instanceof CNull) {
                    throw new CREFormatException("invalid id. Use either a string or integer.", t);
                }
            }

            Static.AssertPlayerNonNull(p, t);
            if (VirtualChests.get(id) != null) {
                p.openInventory(VirtualChests.get(id));
            }
            return CVoid.VOID;
        }

        public String getName() {
            return "popen_virtualchest";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player,] id} Shows the specified inventory to either the"
                    + " specified player, or the player calling the function.";
        }

        public MSVersion since() {
            return MSVersion.V3_3_1;
        }
    }

    @api(environments = {CommandHelperEnvironment.class})
    public static class pget_virtualchest extends AbstractFunction {

        public Class<? extends CREThrowable>[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            MCPlayer p;

            if (args.length == 1) {
                p = Static.GetPlayer(args[0], t);
            } else {
                p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            }

            Static.AssertPlayerNonNull(p, t);

            String id = VirtualChests.getID(p.getOpenInventory().getTopInventory());

            if (id != null) {
                return new CString(id, t);
            }

            return CNull.NULL;

        }

        public String getName() {
            return "pget_virtualchest";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "string {[player]} Returns the id of the virtual chest a player is"
                    + " looking at, or null.";
        }

        public MSVersion since() {
            return MSVersion.V3_3_1;
        }
    }

    @api(environments = {CommandHelperEnvironment.class})
    public static class pviewing_virtualchest extends AbstractFunction {

        public Class<? extends CREThrowable>[] thrown() {
            return new Class[]{CREFormatException.class,
                CRENullPointerException.class};
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            CArray arr = new CArray(t);
            String id = args[0].val();

            if (id.isEmpty() || args[0] instanceof CNull) {
                throw new CREFormatException("invalid id. Use either a string or integer.", t);
            }

            MCInventory inv = VirtualChests.get(id);

            if (inv == null) {
                throw new CRENullPointerException("unknown chest id. Please consult all_virtualchests().", t);
            }

            for (MCHumanEntity p : inv.getViewers()) {
                arr.push(new CString(p.getName(), t), t);
            }

            return arr;
        }

        public String getName() {
            return "pviewing_virtualchest";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "array {id} Returns the playernames of all players viewing a certain chest.";
        }

        public MSVersion since() {
            return MSVersion.V3_3_1;
        }
    }
}
