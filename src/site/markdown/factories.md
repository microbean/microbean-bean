# Factories

When turning something like this:

```
@ApplicationScoped
@C
public class A {

  private B b;

  public A() {
    super();
  }
  
  @Inject
  public void setB(B b) {
    this.b = b;
  }

}
```
…into a `Factory`:
* The _instance type_ is `A`
* The _bean types_ are those of the transitive type closure of the instance type
* The _instance qualifiers_ are `C`
* The _factory class_ is `A`
* The _factory qualifiers_ are `C`
* The _factory executable_ is `A()`
* The _factory executable variables_ are none.
* The _initializer variables_ are the `B`-typed parameter of the `setB(B)` _initializer method_
* The _disposer executable_ is `null`
* The _disposer executable variables_ are none.

Old Byte Buddy-related example:
```
public BBFactoryClassGenerator(final Type productType,
                                 final BeanId beanId,
                                 final Class<?> productFactoryClass, // nullable
                                 final Set<? extends Qualifier> productFactoryQualifiers, // nullable
                                 MethodDescription.Token producerToken, // nullable
                                 final List<? extends Slot> producerDependencies,
                                 Map<? extends ByteCodeElement.Token<?>, ? extends List<Slot>> initializerDependencies,
                                 final Interceptions<TypeDescription.Generic> interceptions,
                                 final MethodDescription.Token disposerToken, // nullable
                                 final List<? extends Slot> disposerDependencies,
                                 final int disposedParameterIndex, // can be negative
                                 final Path directoryForGeneratedClasses) { // nullable
```

