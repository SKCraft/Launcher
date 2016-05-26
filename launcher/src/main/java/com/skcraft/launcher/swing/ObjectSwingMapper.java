/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import com.google.common.base.Strings;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ObjectSwingMapper {

    private final List<FieldMapping> mappings = new ArrayList<FieldMapping>();
    private final Object object;

    public ObjectSwingMapper(@NonNull Object object) {
        this.object = object;
    }

    public void copyFromObject() {
        for (FieldMapping mapping : mappings) {
            mapping.copyFromObject();
        }
    }

    public void copyFromSwing() {
        for (FieldMapping mapping : mappings) {
            mapping.copyFromSwing();
        }
    }

    private void add(@NonNull FieldMapping mapping) {
        mappings.add(mapping);
    }

    private <V> MutatorAccessorField<V> getField(@NonNull String field, Class<V> clazz) {
        return new MutatorAccessorField<V>(object, field, clazz);
    }

    public void map(@NonNull final JTextComponent textComponent, String name) {
        final MutatorAccessorField<String> field = getField(name, String.class);

        add(new FieldMapping() {
            @Override
            public void copyFromObject() {
                textComponent.setText(field.get());
            }

            @SuppressWarnings("unchecked")
            @Override
            public void copyFromSwing() {
                field.set(Strings.emptyToNull(textComponent.getText()));
            }
        });
    }

    public void map(@NonNull final JSpinner spinner, String name) {
        final MutatorAccessorField<Integer> field = getField(name, int.class);

        add(new FieldMapping() {
            @Override
            public void copyFromObject() {
                spinner.setValue(field.get());
            }

            @SuppressWarnings("unchecked")
            @Override
            public void copyFromSwing() {
                field.set((Integer) spinner.getValue());
            }
        });
    }

    public void map(@NonNull final JCheckBox check, String name) {
        final MutatorAccessorField<Boolean> field = getField(name, boolean.class);

        add(new FieldMapping() {
            @Override
            public void copyFromObject() {
                check.setSelected(field.get());
            }

            @SuppressWarnings("unchecked")
            @Override
            public void copyFromSwing() {
                field.set(check.isSelected());
            }
        });
    }

    public static interface FieldMapping {
        void copyFromObject();
        void copyFromSwing();
    }

    public static class MutatorAccessorField<V> {
        private final Class<V> clazz;
        private final Object object;
        private final Method mutator;
        private final Method accessor;

        public MutatorAccessorField(Object object, String name, Class<V> clazz) {
            this.object = object;
            this.clazz = clazz;

            Method mutator = null;
            Method accessor = null;
            for (Method method : object.getClass().getMethods()) {
                if (isAccessor(method, name)) {
                    accessor = method;
                } else if (isMutator(method, name)) {
                    mutator = method;
                }
            }

            if (accessor == null) {
                throw new NoSuchMethodError("Failed to find accessor pair on " +
                        object.getClass().getCanonicalName() + " for " + name);
            }

            if (mutator == null) {
                throw new NoSuchMethodError("Failed to find mutator pair on " +
                        object.getClass().getCanonicalName() + " for " + name);
            }

            this.mutator = mutator;
            this.accessor = accessor;
        }

        private boolean isAccessor(Method method, String name) {
            String methodName = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();

            return (methodName.equalsIgnoreCase("get" + name) ||
                    methodName.equalsIgnoreCase("is" + name)) &&
                    paramTypes.length == 0 &&
                    clazz.isAssignableFrom(returnType);
        }

        private boolean isMutator(Method method, String name) {
            String methodName = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();

            return methodName.equalsIgnoreCase("set" + name) &&
                    paramTypes.length == 1 &&
                    paramTypes[0].isAssignableFrom(clazz);
        }

        @SuppressWarnings("unchecked")
        public V get() {
            try {
                Object value = accessor.invoke(object);
                return (V) value;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        public void set(V value) {
            try {
                mutator.invoke(object, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
