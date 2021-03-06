package com.geotab.sdk.datafeed;

import com.geotab.sdk.datafeed.cli.CommandLineArguments;
import com.geotab.sdk.datafeed.worker.DataFeedWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.MissingArgumentException;

/**
 * This is a console example of obtaining the data feed from the server.
 *
 * <p>1) Process command line arguments: Server, Database, User, Password, Options, File Path and
 * Continuous Feed option.
 *
 * <p>2) Collect data and export it to console or csv.
 *
 * <p>A complete Geotab API object and method reference is available at the Geotab Developer page.
 */
@Slf4j
public class DataFeedApp {

  public static void main(String[] args) throws Exception {

    try {
      CommandLineArguments commandLineArguments = new CommandLineArguments(args);

      DataFeedWorker dataFeedWorker = new DataFeedWorker(commandLineArguments);

      addShutdownHook(dataFeedWorker);

      dataFeedWorker.start();

      if (!commandLineArguments.isFeedContinuously()) {
        while (true) {
          if (dataFeedWorker.isProcessing()) {
            // shutdown only after it started processing
            dataFeedWorker.shutdown();
            break;
          }
        }
      }

      dataFeedWorker.join(); // main thread waits for it to finish

    } catch (MissingArgumentException exception) {
      log.error(exception.getMessage());
    } catch (Exception exception) {
      log.error("Unexpected exception: ", exception);
    } finally {
      log.info("\nPress Enter to exit...");
      System.in.read();
    }

  }

  private static void addShutdownHook(DataFeedWorker dataFeedWorker) {
    final Thread mainThread = Thread.currentThread();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.debug("Application is stopping ...");
      if (dataFeedWorker.isProcessing()) {
        dataFeedWorker.shutdown();
      }
      try {
        mainThread.join(); // waits for main thread to finish
      } catch (InterruptedException e) {
        log.error("Can not join main thread");
      }
      log.debug("Application stopped");
    }));
  }

}
