EAXY - An easy to use XML library for Java
==========================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.eaxy/eaxy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eaxy/eaxy)
[![Build Status](https://travis-ci.org/jhannes/eaxy.png)](https://travis-ci.org/jhannes/eaxy)
[![Coverage Status](https://coveralls.io/repos/jhannes/eaxy/badge.svg?branch=master&service=github)](https://coveralls.io/github/jhannes/eaxy?branch=master)

EAXY is a Java library for building, parsing, searching and manipulating XML.

It is small (< 50 kB) and has no dependencies. It's aim is to have a compact
syntax for doing the most common operations on XML and HTML trees.


Overview
--------

EAXY is build with the following guiding principles:

* Namespaces must be build in from the start
* Utility methods are good
* Links to parent nodes and document complicates matters
* Libraries should not have dependencies



Building XML documents:
-----------------------
        Namespace A_NS = new Namespace("uri:a", "a");
        Namespace B_NS = new Namespace("uri:b", "b");

        Element xml = A_NS.el("root",
                Xml.el("node-without-namespace", "element text"), 
                A_NS.el("parent", 
                        A_NS.el("child")),
                B_NS.el("other-parent", Xml.text("Here's some text")));


Manipulating XML documents:
---------------------------
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


Finding contents of XML documents:
---------------------------------
        Element xml = el("div",
                el("div").id("not-here").add(text("something")),
                el("div").id("below-here").add(
                        el("div", el("div", el("p", text("around "), el("span", "HERE"), text(" around"))))));
        assertThat(xml.find("...", "#below-here", "...", "p", "...").first().text())
            .isEqualTo("HERE");


HTML utilities:
---------------
        Element form = el("form",
                el("input").id("first_name_id").name("first_name").type("text").val("Darth"),
                el("input").id("last_name_id").name("last_name").type("text").val("Vader"),
                el("input").name("createPerson").type("submit").val("Create person"));
        assertThat(form.find("input").ids()).containsExactly("first_name_id", "last_name_id");
        assertThat(form.find("input").values()).containsExactly("Darth", "Vader", "Create person");
        assertThat(form.find("input").names()).containsExactly("first_name", "last_name", "createPerson");

        assertThat(form.find("input").first().val()).isEqualTo("Darth");


Known issues:
-------------
* SAX namespace may be broken with certain classpath combinations - perhaps I need to write my own parser
* Finding doesn't include the current element, which is often non-intuitive
* The "..." syntax is unintuitive and could use some improvement
