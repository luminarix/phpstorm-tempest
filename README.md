# Tempest support for PHPStorm

## Features
- Views
    - Syntax highlighting for
        - `{{ $var }}`
        - `{{!! $var !!}}`
        - `{{-- comment --}}`
    - Autocompletion
        - On space*
            - `{{ $var }}`
            - `{{!! $var !!}}`
            - `{{-- comment --}}`
        - Control flow directives
            - `:if`
            - `:elseif`
            - `:else`
            - `:foreach`
            - `:forelse`
- Go to view from controllers
*: for example, write `{{`, press space and the plugin will autocomplete the `}}` part.

Download the latest `.jar` release from the [releases page](https://github.com/xHeaven/phpstorm-tempest/releases) (or build it yourself with `gradlew build`) and install it from the disk as shown on the picture below.

![alt text](<assets/installl-from-disk.png>)