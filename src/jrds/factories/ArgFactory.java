/*##########################################################################
 _##
 _##  $Id: ProbeFactory.java 373 2006-12-31 00:06:06Z fbacchella $
 _##
 _##########################################################################*/

package jrds.factories;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jrds.Util;
import jrds.factories.xml.JrdsElement;

import org.apache.log4j.Logger;

/**
 * A class to build args from a string constructor
 * @author Fabrice Bacchella 
 * @version $Revision: 373 $,  $Date: 2006-12-31 01:06:06 +0100 (Sun, 31 Dec 2006) $
 */

public final class ArgFactory {
    static private final Logger logger = Logger.getLogger(ArgFactory.class);

    static private final String[] argPackages = new String[] {"java.lang.", "java.net.", "org.snmp4j.smi.", "java.io", ""};

    /**
     * This method build a list from an XML enumeration of element.
     * 
     * The enumeration is made of :<p/>
     * <code>&lt;arg type="type" value="value"></code><p/>
     * or<p/>
     * <code>&lt;arg type="type">value&lt;/value></code><p/>
     * This method is recursive, so it if finds some <code>list</code> elements instead of an <code>arg</code>, it will build a sub-list.
     * 
     * Unknown element will be silently ignored.
     * 
     * @param sequence an XML element that contains as sequence of <code>arg</code> or <code>list</code> elements.
     * @param arguments some object that will be used by a call to <code>jrds.Util.parseTemplate</code> for the arg values
     * @return
     */
    public static List<Object> makeArgs(JrdsElement sequence, Object... arguments) {
        List<JrdsElement> elements = sequence.getChildElements();
        List<Object> argsList = new ArrayList<Object>(elements.size());
        for(JrdsElement listNode: elements) {
            String localName = listNode.getNodeName();
            logger.trace(Util.delayedFormatString("Element to check: %s", localName));
            if("arg".equals(localName)) {
                String type = listNode.getAttribute("type");
                String value = null;
                if(listNode.hasAttribute("value"))
                    value = listNode.getAttribute("value");
                else
                    value = listNode.getTextContent();
                value = jrds.Util.parseTemplate(value, arguments);
                Object o = ArgFactory.makeArg(type, value);
                argsList.add(o);                
            }
            else if("list".equals(localName)) {
                argsList.add(makeArgs(listNode, arguments));                
            }
        }
        logger.debug(Util.delayedFormatString("arg vector: %s", argsList));
        return argsList;
    }

    /**
     * Create an objet providing the class name and a String argument. So the class must have
     * a constructor taking only a string as an argument.
     * @param className
     * @param value
     * @return
     */
    public static final Object makeArg(String className, String value) {
        Object retValue = null;
        Class<?> classType = resolvClass(className);
        if (classType != null) {
            Class<?>[] argsType = { String.class };
            Object[] args = { value };

            try {
                Constructor<?> theConst = classType.getConstructor(argsType);
                retValue = theConst.newInstance(args);
            }
            catch (Exception ex) {
                logger.warn("Error during of creation :" + className + ": ", ex);
            }
        }
        return retValue;
    }

    private static final Class<?> resolvClass(String name) {
        Class<?> retValue = null;
        for (String packageTry: argPackages) {
            try {
                retValue = Class.forName(packageTry + name);
            }
            catch (ClassNotFoundException ex) {
            }
            catch (NoClassDefFoundError ex) {
            }
        }
        if (retValue == null)
            logger.warn("Class " + name + " not found");
        return retValue;
    }

    public static Object ConstructFromString(Class<?> clazz, String value) throws InvocationTargetException {
        try {
            Constructor<?> c = null;
            if(! clazz.isPrimitive() ) {
                c = clazz.getConstructor(String.class);
            }
            else if(clazz == Integer.TYPE) {
                c = Integer.class.getConstructor(String.class);
            }
            else if(clazz == Double.TYPE) {
                c = Double.class.getConstructor(String.class);
            }
            else if(clazz == Float.TYPE) {
                c = Float.class.getConstructor(String.class);
            }
            return c.newInstance(value);
        } catch (SecurityException e) {
            throw new InvocationTargetException(e, clazz.getName());
        } catch (NoSuchMethodException e) {
            throw new InvocationTargetException(e, clazz.getName());
        } catch (IllegalArgumentException e) {
            throw new InvocationTargetException(e, clazz.getName());
        } catch (InstantiationException e) {
            throw new InvocationTargetException(e, clazz.getName());
        } catch (IllegalAccessException e) {
            throw new InvocationTargetException(e, clazz.getName());
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e, clazz.getName());
        }
    }

    static public void beanSetter(Object o, Map<String, PropertyDescriptor> beanProperties, String beanName, String beanValue) throws InvocationTargetException{
        try {
            PropertyDescriptor pd = beanProperties.get(beanName);
            Method setMethod = pd.getWriteMethod();
            if(setMethod == null) {
                throw new InvocationTargetException(new NullPointerException(), String.format("Unknown bean %s", beanName));
            }
            Class<?> setArgType = pd.getPropertyType();
            Object argInstance = ArgFactory.ConstructFromString(setArgType, beanValue);
            setMethod.invoke(o, argInstance);       
        } catch (NullPointerException e) {
            throw new InvocationTargetException(e, beanName);
        } catch (SecurityException e) {
            throw new InvocationTargetException(e, beanName);
        } catch (IllegalArgumentException e) {
            throw new InvocationTargetException(e, beanName);
        } catch (IllegalAccessException e) {
            throw new InvocationTargetException(e, beanName);
        }
    }

    static public Map<String, PropertyDescriptor> getBeanPropertiesMap(Class<?> c) throws InvocationTargetException {
        try {
            BeanInfo bi = Introspector.getBeanInfo(c);
            Map<String, PropertyDescriptor> beanProperties = new HashMap<String, PropertyDescriptor>();
            for(PropertyDescriptor pd: bi.getPropertyDescriptors()) {
                beanProperties.put(pd.getName(), pd);
            }
            return beanProperties;
        } catch (IntrospectionException e) {
            throw new InvocationTargetException(e, c.getName());
        }
    }
    
    /**
     * Enumerate the hierarchy of annotation for a class, until a certain class type is reached
     * @param searched the Class where the annotation is searched
     * @param annontationClass the annotation class
     * @param stop a class that will stop (included) the search 
     * @return
     */
    static public <T extends Annotation> Set<T> enumerateAnnotation(Class<?> searched, Class<T> annontationClass, Class<?> stop) {
        Set<T> annotations =  new HashSet<T>();
        while(searched != null && stop.isAssignableFrom(searched)) {
            if(searched.isAnnotationPresent(annontationClass)) {
                T annotation = searched.getAnnotation(annontationClass);
                annotations.add(annotation);
            }
            searched = searched.getSuperclass();
        }
        return annotations;
    }

}
