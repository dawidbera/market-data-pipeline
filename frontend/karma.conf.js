    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-gpu']
      }
    },
    client: {
      clearContext: false,
      jasmine: {
        // you can add configuration options for Jasmine here
        // the possible options are listed at https://jasmine.github.io/api/edge/Configuration.html
        // for example, you can disable the random execution with `random: false`
        // or set a specific seed with `seed: 4321`
      },
      // Needed for Angular 18+ tests that use standalone components without explicit imports in spec files
      clearContext: false // leave Jasmine Spec Runner output visible in browser
    },
    jasmineHtmlReporter: {
      suppressAll: true // removes the duplicated traces
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/frontend'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' }
      ]
    },
    reporters: ['progress', 'kjhtml'],
    browsers: ['ChromeHeadlessNoSandbox'],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-gpu']
      }
    },
    restartOnFileChange: true,
    // Add specific TS options for Karma
    // This ensures correct module resolution and compatibility for tests.
    browserDisconnectTimeout: 10000, // default 2000
    browserDisconnectTolerance: 1, // default 1
    browserNoActivityTimeout: 4*60*1000, //default 10000
    captureTimeout: 60000, // default 60000
    reportSlowerThan: 500, // report tests slower than 500ms
    colors: true,
    logLevel: config.LOG_INFO,
    singleRun: true,
    autoWatch: false,
    preprocessors: {
      'src/**/*.js': ['coverage']
    },
    // Karma Istanbul reporter configuration
    coverageIstanbulReporter: {
      reports: ['html', 'lcovonly', 'text-summary'],
      fixWebpackSourcePaths: true
    },
    // TS options for Karma
    // These are applied to the files being tested
    // Note: These options might need to align with tsconfig.json or be specific to Karma.
    // For simplicity, we'll keep them minimal here.
    // 'typescript': {
    //   typescriptOptions: {
    //     module: 'es2020',
    //     target: 'es2020',
    //     moduleResolution: 'node',
    //     sourceMap: true,
    //     emitDecoratorMetadata: true
    //   }
    // }
  });
};
