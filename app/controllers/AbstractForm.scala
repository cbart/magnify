package controllers

import play.api.data.{Form, Mapping}

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
abstract class AbstractForm[T] (mapping: Mapping[T]) extends Form(mapping, Map.empty, Nil, None)