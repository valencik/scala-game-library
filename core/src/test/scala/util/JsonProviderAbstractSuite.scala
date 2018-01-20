package sgl.util

import org.scalatest.FunSuite

trait JsonProviderAbstractSuite extends FunSuite with JsonProvider {

  import Json._

  test("Parsing literals should return the right literal") {
    parse("42") match {
      case JNumber(n) => assert(n === 42)
      case _ => assert(false)
    }
    parse("12.34") match {
      case JNumber(n) => assert(n === 12.34)
      case _ => assert(false)
    }
    parse("\"abc\"") match {
      case JString(v) => assert(v === "abc")
      case _ => assert(false)
    }
    parse("true") match {
      case JBoolean(b) => assert(b)
      case _ => assert(false)
    }
    assert(parse("null") === JNull)
  }

  test("AsInt extracts int when possible") {
    parse("23") match {
      case AsInt(n) => assert(n === 23)
      case _ => assert(false)
    }
    parse("10") match {
      case AsInt(n) => assert(n === 10)
      case _ => assert(false)
    }
  }

  test("AsInt should not extract an int from a true double") {
    parse("12.34") match {
      case AsInt(n) => assert(false)
      case _ => assert(true)
    }
  }

  test("Parsing simple objects should be extractable as a JObject") {
    parse("{\"x\": 23}") match {
      case JObject(List(("x", JNumber(n)))) => assert(n === 23)
      case _ => assert(false)
    }
    parse("{\"y\": \"abc\"}") match {
      case JObject(List(("x", _))) => assert(false)
      case JObject(List(("y", JString(v)))) => assert(v === "abc")
      case _ => assert(false)
    }
    parse("{\"z\": 12.34}") match {
      case JNumber(_) => assert(false)
      case JArray(_) => assert(false)
      case JObject(List(("z", JNumber(d)))) => assert(d === 12.34)
      case _ => assert(false)
    }
  }

  test("Simple parsed objects can be navigated with \\") {
    val o1 = parse("{\"x\": 12}")
    (o1 \ "x") match {
      case JNumber(n) => assert(n === 12)
      case _ => assert(false)
    }
  }

  test("Accessing a non-existing field of an object with \\ returns a JNothing") {
    val o1 = parse("{\"x\": 12}")
    assert(o1 \ "y" === JNothing)
    assert(o1 \ "x" !== JNothing)
  }

  test("Parsing simple array should return a JArray with the right elements") {
    parse("[1]") match {
      case JArray(List(JNumber(n))) => assert(n === 1)
      case _ => assert(false)
    }
    parse("[\"abc\"]") match {
      case JArray(List(JString(v))) => assert(v === "abc")
      case _ => assert(false)
    }
    parse("[12.34]") match {
      case JNumber(_) => assert(false)
      case JObject(_) => assert(false)
      case JArray(List(JNumber(d))) => assert(d === 12.34)
      case _ => assert(false)
    }
  }

  test("Parsing composed objects") {
    parse("{\"abc\": [1, \"xxx\", true]}") match {
      case JObject(List(("abc", 
             JArray(List(
               JNumber(n),
               JString(s),
               JBoolean(b)))))) => {
        assert(n === 1)
        assert(s === "xxx")
        assert(b === true)
      }
      case _ => assert(false)
    }
  }

  test("Parsing double literals that are actually integers should still return doubles") {
    parse("1.0") match {
      case JNumber(n) => assert(n === 1)
      case _ => assert(false)
    }
    assert(parse("1.0") === parse("1"))
  }

  test("Parsing way too large literal should approximate to the closest double") {
    parse("33333333333333333333333") match {
      case AsInt(_) => assert(false)
      case JNumber(_) => assert(true)  // exact value is annoying to test.
      case _ => assert(false)
    }
    parse("99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999") match {
      case AsInt(_) => assert(false)
      case JNumber(v) => assert(v == Double.PositiveInfinity)
      case _ => assert(false)
    }
    parse("99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999.12345") match {
      case JNumber(v) => assert(v == Double.PositiveInfinity)
      case _ => assert(false)
    }
  }

}
