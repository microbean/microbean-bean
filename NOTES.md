# Notes

## Factories, Producers, etc.

A `Factory` is the user-visible creation mechanism.

It is usually comprised of:
* A `Producer`. This creates the product and resolves its dependencies. This also performs constructor interception.
** This _may_ (haven't decided yet) decompose into something that does the resolution, and the "instantiator", whose job
   is simply to `new`.
* An `Initializer`. This calls the product's initializer methods.
* A `PostInitializer`. This calls the product's `postConstruct()` callbacks.
* A `PreDestructor`. This calls the product's `preDestroy()` callbacks.
