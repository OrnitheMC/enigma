# Enigma

A tool for deobfuscation of Java bytecode. Forked from <https://bitbucket.org/cuchaz/enigma>, copyright Jeff Martin.
Enigma is used by Orinthe to remap, decompile and deobfuscate minecraft source code.
This project was forked from QuiltMCs version of Enigma which was in turn forked from FabricMCs Enigma.

## License

Enigma is distributed under the [LGPL-3.0](LICENSE).

Enigma includes the following open-source libraries:
 - A [modified version](https://github.com/FabricMC/procyon) of [Procyon](https://bitbucket.org/mstrobel/procyon) (Apache-2.0)
 - A [modified version](https://github.com/FabricMC/cfr) of [CFR](https://github.com/leibnitz27/cfr) (MIT)
 - [Guava](https://github.com/google/guava) (Apache-2.0)
 - [SyntaxPane](https://github.com/Sciss/SyntaxPane) (Apache-2.0)
 - [FlatLaf](https://github.com/JFormDesigner/FlatLaf) (Apache-2.0)
 - [jopt-simple](https://github.com/jopt-simple/jopt-simple) (MIT)
 - [ASM](https://asm.ow2.io/) (BSD-3-Clause)

## Usage

Pre-compiled jars can be found on the [Ornithe maven](https://maven.ornithemc.net/).

### Launching the GUI

`java -jar enigma.jar`

### On the command line

`java -cp enigma.jar cuchaz.enigma.command.Main`

### Contributing
1. Clone the project
2. Start editing
3. You can launch enigma using the following command to test your changes:
```
./gradlew :enigma-swing:run --args="-jar <jar to analyse location> -mappings <mappings location> -profile <enigma_profile.json lowation>"
```
Note that any of the arguments after `--args` are optional
