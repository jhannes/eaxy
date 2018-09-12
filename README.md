EAXY - An easy to use XML library for Java
==========================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.eaxy/eaxy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eaxy/eaxy)
[![Build Status](https://travis-ci.org/jhannes/eaxy.png)](https://travis-ci.org/jhannes/eaxy)
[![Coverage Status](https://coveralls.io/repos/jhannes/eaxy/badge.svg?branch=master&service=github)](https://coveralls.io/github/jhannes/eaxy?branch=master)

EAXY is a fluent Java library for building, parsing, searching and manipulating XML. It
aims to have a compact syntax for doing the most common operations on XML and HTML trees.
It is designed to be pleasant to use, both when you work with simple and small XML
files and when you work with huge, poorly structured or deeply nested XML files where code
generation is not feasible.

It's very suitable for building XML structures to use in unit tests.

Eaxy is small (< 100 kB) and has no dependencies. 


Overview
--------

EAXY is build with the following guiding principles:

* Namespaces must be build in from the start
* Links to parent nodes and document complicates the design and imposes problems with copying subtres
* Libraries should prefer reimplementing trivial code over adding dependencies

Features:
--------

1. Parse large XML documents (a 90 MB document in 3 seconds!):
   `for (Element subtree : Xml.filter("html", "body", "ul", "li").iterate(reader))`
2. Build complex XML documents with a simple internal DSL
   `Xml.el("html", Xml.el("body", Xml.el("h4", "A message")))`
3. Flexibly traverse complex XML documents
   `doc.find("body", "h4").check().text(); doc.find("body", "ul", "li").texts()`
4. Experimental: Random XML services for testing
   `SoapSimulatorServer server = new SoapSimulatorServer(10080); server.addSoapEndpoint(...); server.start();`

Design:
-------

![Class diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.github.com/jhannes/eaxy/master/doc/classes.puml)


Building XML documents:
-----------------------
```java
Namespace A_NS = new Namespace("uri:a", "a");
Namespace B_NS = new Namespace("uri:b", "b");

Element xml = A_NS.el("root",
        Xml.el("node-without-namespace", "element text"), 
        A_NS.el("parent", 
                A_NS.el("child")),
        B_NS.el("other-parent", Xml.text("Here's some text")));
```

Manipulating XML documents:
---------------------------
```java
Element div = el("div");
assertThat(div.className()).isNull();
div.addClass("important");
div.addClass("hidden");
assertThat(div.className()).isEqualTo("important hidden");
div.removeClass("hidden");
div.removeClass("hidden");
assertThat(div.className()).isEqualTo("important");
div.removeClass("important");
assertThat(div.className()).isEmpty();
```

Finding contents in XML documents:
---------------------------------
```java
Element xml = el("div",
        el("div").id("not-here").add(text("something")),
        el("div").id("below-here").add(
                el("div", el("div", el("p", text("around "), el("span", "HERE"), text(" around"))))));
assertThat(xml.find("...", "#below-here", "...", "p", "...").first().text())
    .isEqualTo("HERE");
```


Streaming large files:
----------------------

```java
ElementQuery filter = Xml.filter("parentElement", "childElement");
try (Reader reader = new FileReader(hugeFile)) {
    for (Element element : filter.iterate(reader)) {
        if ("active".equals(element.find("status").first().attr("value"))) {
            System.out.println(element.find("description", "name").first().text());
        }
    }
}
```


HTML utilities:
---------------
```java
Element form = el("form",
        el("input").id("first_name_id").name("first_name").type("text").val("Darth"),
        el("input").id("last_name_id").name("last_name").type("text").val("Vader"),
        el("input").name("createPerson").type("submit").val("Create person"));
assertThat(form.find("input").ids()).containsExactly("first_name_id", "last_name_id");
assertThat(form.find("input").values()).containsExactly("Darth", "Vader", "Create person");
assertThat(form.find("input").names()).containsExactly("first_name", "last_name", "createPerson");

assertThat(form.find("input").first().val()).isEqualTo("Darth");
```

Generating sample XMLs from XSD (experimental):
-----------------------------------------------
```java
SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder("xsd/StockQuoteService.wsdl");
Element output = builder
     .service("StockQuoteService")
     .operation("GetLastTradePrice")
     .randomOutput("m");
assertThat(output.tagName()).isEqualTo("TradePrice");
double price = Float.parseFloat(output.find("price").single().text());


SampleXmlBuilder generator = new SampleXmlBuilder(getClass().getResource("/xsd/po.xsd"), "po");
generator.setFull(true);
Element el = generator.createRandomElement("purchaseOrder");
assertThat(el.hasAttr("orderDate")).isTrue();
assertThat(el.find("comment")).isNotEmpty();
assertThat(el.find("items").check().find("item")).isNotEmpty();
assertThat(el.find("shipTo").single().attr("country")).isEqualTo("US");
generator.getValidator().validate(el);
```



Known issues:
=============
* SAX namespace may be broken with certain classpath combinations - perhaps I need to write my own parser
* Finding doesn't include the current element, which is often non-intuitive
* The "..." syntax is unintuitive and could use some improvement
