{ pkgs ? import <nixpkgs> {} }:
pkgs.mkShell {
  buildInputs = [
    # Javascript
    pkgs.nodejs
    pkgs.yarn

    # Misc
    pkgs.babashka
  ];
}
