package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class Cuenta {

  private double saldo;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  } //Constructor innecesario, saldo ya inicializado por defecto

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    validarDeposito(cuanto);
    enviarMovimiento( cuanto, true);
  }

  private void validarDeposito(double cuanto){
    validarSiEsValorNegativo(cuanto);
    validarCantidadDeDepositos();
  }

  private void validarCantidadDeDepositos() {
    if(this.superaLosTresDepositos()){
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  private void validarSiEsValorNegativo(double cuanto) {
    if(this.valorNegativo(cuanto)){
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void enviarMovimiento(double cuanto, boolean tipoMovimiento){
    Movimiento movimiento = new Movimiento(LocalDate.now(), cuanto, tipoMovimiento);
    movimiento.agregateA(this);
  }

  private boolean superaLosTresDepositos(){
    return getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count() >= 3;
  }

  private boolean valorNegativo(double cuanto){
    return cuanto <= 0;
  }

  public void sacar(double cuanto) {
    validarExtracciones(cuanto);
    enviarMovimiento(cuanto, false);
  }

  private void validarExtracciones(double cuanto) {
    validarSiEsValorNegativo(cuanto);
    validarSaldoMenorAExtraccion(cuanto);
    validarMaximoDeEstradicciones(cuanto);
  }

  private void validarMaximoDeEstradicciones(double cuanto) {
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
              + " diarios, límite: " + limite);
    }
  }

  private void validarSaldoMenorAExtraccion(double cuanto) {
    if (saldo - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }


  public void registrarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    Stream<Movimiento> depositosDeFecha = getMovimientos().stream()
            .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha));
    return depositosDeFecha.mapToDouble(Movimiento::getMonto); //No me funciona el tipo de dato
    /*
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))//delegar
        .mapToDouble(Movimiento::getMonto)
        .sum();*/
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
